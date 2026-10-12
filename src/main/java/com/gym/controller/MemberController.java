package com.gym.controller;

import com.gym.entity.Member;
import com.gym.entity.MemberCard;
import com.gym.repository.MemberCardRepository;
import com.gym.repository.MemberRepository;
import com.gym.repository.UserAccountRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 会员管理：列表 / 新增 / 编辑 / 会员卡
 */
@Controller
@RequestMapping("/member")
public class MemberController {

    private final MemberRepository memberRepository;
    private final MemberCardRepository cardRepository;
    private final UserAccountRepository userRepository;

    public MemberController(MemberRepository memberRepository,
                            MemberCardRepository cardRepository,
                            UserAccountRepository userRepository) {
        this.memberRepository = memberRepository;
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
    }

    /** 列表（含搜索和剩余课时汇总） */
    @GetMapping("/list")
    public String list(@RequestParam(defaultValue = "") String kw,
                       @RequestParam(defaultValue = "0") int page, HttpSession session, Model model) {
        requireRole(session, "admin", "front");
        Page<Member> memberPage = memberRepository.search(kw.trim(), PageRequest.of(Math.max(page, 0), 10));
        List<Member> members = memberPage.getContent();
        model.addAttribute("members", members);
        model.addAttribute("pageData", memberPage);
        model.addAttribute("currentPage", memberPage.getNumber());
        model.addAttribute("pagePath", "/member/list");
        model.addAttribute("kw", kw);
        // 剩余课时
        Map<String, Integer> remainMap = new HashMap<>();
        for (Member m : members) {
            List<MemberCard> cards = cardRepository.findByMemberIdAndStatus(m.getMemberId(), "有效");
            remainMap.put(m.getMemberId(), cards.stream().mapToInt(MemberCard::getRemainingTimes).sum());
        }
        model.addAttribute("remainMap", remainMap);
        return "member/list";
    }

    @GetMapping("/add")
    public String addPage(HttpSession session) {
        requireRole(session, "admin", "front");
        return "member/add";
    }

    @PostMapping("/add")
    @Transactional
    public String doAdd(@RequestParam String name,
                        @RequestParam String gender,
                        @RequestParam String phone,
                        @RequestParam(required = false) String birthday,
                        @RequestParam(required = false) String address,
                        @RequestParam(required = false) String remark,
                        HttpSession session,
                        Model model) {
        requireRole(session, "admin", "front");
        // 生成会员编号 M + 4位随机数
        String mid = "M" + String.format("%04d", ThreadLocalRandom.current().nextInt(1, 10000));
        Member m = new Member();
        m.setMemberId(mid);
        m.setName(name);
        m.setGender(gender);
        m.setPhone(phone);
        m.setBirthday(birthday == null || birthday.isEmpty() ? null : LocalDate.parse(birthday));
        m.setRegisterDate(LocalDate.now());
        m.setStatus("正常");
        m.setAddress(address);
        m.setRemark(remark);
        try {
            memberRepository.save(m);
            // 自动创建登录账号（member + 编号数字，密码 123456）
            String uname = "member" + mid.substring(1);
            userRepository.save(buildAccount(uname, mid, name));
            model.addAttribute("msg", "会员添加成功！登录账号：" + uname + " / 密码：123456");
        } catch (Exception e) {
            model.addAttribute("msg", "添加失败：" + e.getMessage());
        }
        return "member/add";
    }

    @GetMapping("/edit")
    public String editPage(@RequestParam String id, HttpSession session, Model model) {
        requireRole(session, "admin", "front");
        model.addAttribute("member", memberRepository.findById(id).orElseThrow());
        return "member/edit";
    }

    @PostMapping("/edit")
    public String doEdit(@RequestParam String id,
                         @RequestParam String name,
                         @RequestParam String gender,
                         @RequestParam String phone,
                         @RequestParam(required = false) String birthday,
                         @RequestParam String status,
                         @RequestParam(required = false) String address,
                         @RequestParam(required = false) String remark,
                         HttpSession session) {
        requireRole(session, "admin", "front");
        Member m = memberRepository.findById(id).orElseThrow();
        m.setName(name);
        m.setGender(gender);
        m.setPhone(phone);
        m.setBirthday(birthday == null || birthday.isEmpty() ? null : LocalDate.parse(birthday));
        m.setStatus(status);
        m.setAddress(address);
        m.setRemark(remark);
        memberRepository.save(m);
        return "redirect:/member/list";
    }

    /** 会员卡管理页 */
    @GetMapping("/card")
    public String cardPage(@RequestParam String id, HttpSession session, Model model) {
        requireRole(session, "admin", "front");
        model.addAttribute("member", memberRepository.findById(id).orElseThrow());
        model.addAttribute("cards", cardRepository.findByMemberIdOrderByCardIdDesc(id));
        return "member/card";
    }

    /** 办理新卡 */
    @PostMapping("/card")
    public String doAddCard(@RequestParam String id,
                            @RequestParam String cardType,
                            @RequestParam String startDate,
                            @RequestParam(required = false, defaultValue = "0") int totalTimes,
                            HttpSession session) {
        requireRole(session, "admin", "front");
        int days = switch (cardType) {
            case "月卡" -> 30;
            case "季卡" -> 90;
            default -> 365; // 年卡/次卡
        };
        LocalDate start = LocalDate.parse(startDate);
        MemberCard card = new MemberCard();
        card.setMemberId(id);
        card.setCardType(cardType);
        card.setStartDate(start);
        card.setEndDate(start.plusDays(days));
        int times = "次卡".equals(cardType) ? totalTimes : 0;
        card.setTotalTimes(times);
        card.setRemainingTimes(times);
        card.setStatus("有效");
        cardRepository.save(card);
        return "redirect:/member/card?id=" + id;
    }

    /** 冻结会员卡 */
    @GetMapping("/card/freeze")
    public String freezeCard(@RequestParam Integer cardId, @RequestParam String id, HttpSession session) {
        requireRole(session, "admin", "front");
        cardRepository.findById(cardId).ifPresent(c -> {
            c.setStatus("冻结");
            cardRepository.save(c);
        });
        return "redirect:/member/card?id=" + id;
    }

    private com.gym.entity.UserAccount buildAccount(String uname, String refId, String realName) {
        com.gym.entity.UserAccount u = new com.gym.entity.UserAccount();
        u.setUsername(uname);
        u.setPassword(com.gym.util.MD5Util.md5("123456"));
        u.setRole("member");
        u.setRefId(refId);
        u.setRealName(realName);
        u.setStatus("正常");
        return u;
    }

    private void requireRole(HttpSession session, String... roles) {
        String role = (String) session.getAttribute("role");
        for (String r : roles) {
            if (r.equals(role)) return;
        }
        throw new IllegalStateException("权限不足，无法访问此页面。");
    }
}
