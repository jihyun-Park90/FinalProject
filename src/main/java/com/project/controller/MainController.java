
package com.project.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.project.model.MessageItem;
import com.project.model.NoticeItem;
import com.project.model.response.LoginResponse;
import com.project.service.MainSO;

import jakarta.servlet.http.HttpSession;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/")
public class MainController {
	@Autowired
	MainSO mainSO;
	String viewPath;

	@GetMapping("/goCourseHome")
	public String goCourseHome(@RequestParam(required = true, name = "courseId") int courseId, HttpSession session) {
		session.setAttribute("currentId", courseId);

		return "redirect:/home";
	}

	@GetMapping("/")
	public String getMain(Model model, @RequestParam(required = false, defaultValue = "1", name = "page") String page,
			HttpSession session) {
		int memberId, memberRole;
		LoginResponse auth = (LoginResponse) session.getAttribute("auth");

		try {
			memberId = auth.getMember_id();
			memberRole = auth.getM_role();

			if (memberRole == 1) {
				model.addAttribute("course", mainSO.selectByMemberId(memberId, Integer.parseInt(page)));
				model.addAttribute("notice", mainSO.selectNoticeItems(1, 5));
				model.addAttribute("size", mainSO.getSizeByMemberId(memberId));
				viewPath = "main/index";
			} else {
				model.addAttribute("notice", mainSO.selectNoticeItems(1, 5));
				viewPath = "main/index_i";
			}
			model.addAttribute("page", page);
			model.addAttribute("menu", "main");
			return viewPath;
		} catch (Exception e) {
			return "redirect: /login";
		}
	}

	@GetMapping("/checkin")
	public String getCheckin(Model model, HttpSession session) {
		int memberId, studentId, courseId, memberRole;
		LoginResponse auth = (LoginResponse) session.getAttribute("auth");

		try {
			memberId = auth.getMember_id();
			memberRole = auth.getM_role();

			if (memberRole == 1) {
				studentId = mainSO.checkCourseForStudentId(memberId);
				if (studentId > 0) {
					model.addAttribute("info", mainSO.getInfoByStudentId(studentId));
					model.addAttribute("stats", mainSO.getStats(studentId));
					model.addAttribute("time", mainSO.getTimetable(studentId));
					viewPath = "main/checkin";
				}
			} else {
				courseId = mainSO.checkCourseForCourseId(memberId);
				if (courseId > 0) {
					model.addAttribute("info", mainSO.getInfoByCourseId(courseId));
					viewPath = "main/checkin_i";
				}
			}
			model.addAttribute("menu", "checkin");
			return viewPath;
		} catch (Exception e) {
			return "redirect: /login";
		}
	}

	@GetMapping("/checkin/createQR")
	public String createQR(Model model, HttpSession session) {
		int memberId;

<<<<<<< HEAD
		return "redirect:/checkin";
=======
	@ResponseBody
	@GetMapping("/api/checkin/createQR")
	public MessageItem createQR(Model model, HttpSession session) {
		int memberId, courseId;
		MessageItem response = new MessageItem();
		try {
			memberId = ((LoginResponse) session.getAttribute("auth")).getMember_id();
			courseId = mainSO.checkCourseForCourseId(memberId);

			CourseItem courseItem = mainSO.getQrCode(courseId, new CourseItem());

			if (courseItem.getQrCode() == null) {
				try {
					String encryptedText = qrSO.getEncryptedText("test");
					mainSO.createQR(courseId, encryptedText);
					response.setRes(true);
					response.setMsg("QR코드가 성공적으로 생성되었습니다.");
				} catch (Exception e) {
					response.setRes(false);
					response.setMsg("QR 코드 생성 과정에서 오류가 발생하였습니다.");
				}
			} else {
				response.setRes(false);
				response.setMsg("현재 유효한 QR코드가 있어 생성이 불가능합니다.");
			}
		} catch (Exception e) {
			response.setRes(false);
			response.setMsg("잘못된 접근입니다.");
		}
		return response;
>>>>>>> 0497dbd (기타: 로그인 기능 연결)
	}

	@ResponseBody
	@GetMapping("/api/checkin/update")
	public MessageItem updateTimetable(@RequestParam(required = true, name = "keyword") String keyword,
			@RequestParam(required = true, name = "code") String code, HttpSession session) {
		int memberId, studentId;
		MessageItem response = new MessageItem();

		try {
			memberId = ((LoginResponse) session.getAttribute("auth")).getMember_id();
			studentId = mainSO.checkCourseForStudentId(memberId);

			response.setRes(mainSO.isQRValid(studentId, code));

			if (response.isRes()) {
				response.setRes(mainSO.updateTimetable(studentId, keyword) > 0);
				if (!response.isRes()) {
					response.setMsg("출석체크 요청이 처리되지 않았습니다.");
				}
			} else {
				response.setMsg("QR코드가 유효하지 않습니다.");
			}
		} catch (Exception e) {
			response.setRes(false);
			response.setMsg("잘못된 접근입니다.");
		}
		return response;
	}

	@GetMapping("/register")
	public String getCourseRegisteration(Model model,
			@RequestParam(required = false, defaultValue = "1", name = "page") String page) {
		model.addAttribute("list", mainSO.selectByDates(Integer.parseInt(page)));
		model.addAttribute("size", mainSO.getSizeByDates());
		model.addAttribute("page", page);
		model.addAttribute("menu", "register");
		return "main/register";
	}

	@RequestMapping("/register/search")
	public String searchCourse(Model model,
			@RequestParam(required = false, defaultValue = "1", name = "page") String page,
			@RequestParam(required = true, name = "keyword") String keyword) {
		model.addAttribute("list", mainSO.selectByDates(keyword, Integer.parseInt(page)));
		model.addAttribute("size", mainSO.getSizeByDates(keyword));
		model.addAttribute("page", page);
		model.addAttribute("menu", "register");
		model.addAttribute("keyword", keyword);
		return "main/register_search";
	}

	@GetMapping("/notification")
	public String getNotifications(Model model) {
		model.addAttribute("menu", "alert");
		return "main/alert";
	}

	@GetMapping("/notice")
	public String getNotice(Model model,
			@RequestParam(required = false, defaultValue = "1", name = "page") String page) {
		model.addAttribute("list", mainSO.selectNoticeItems(Integer.parseInt(page)));
		model.addAttribute("size", mainSO.getSize());
		model.addAttribute("page", page);
		model.addAttribute("menu", "notice");
		return "main/notice";
	}

	@RequestMapping("/notice/search")
	public String searchNotice(Model model,
			@RequestParam(required = false, defaultValue = "1", name = "page") String page,
			@RequestParam(required = true, name = "keyword") String keyword) {
		model.addAttribute("list", mainSO.selectNoticeItems(Integer.parseInt(page), keyword));
		model.addAttribute("size", mainSO.getSize(keyword));
		model.addAttribute("page", page);
		model.addAttribute("menu", "notice");
		model.addAttribute("keyword", keyword);
		return "main/notice_search";
	}

	@ResponseBody
	@GetMapping("/api/notice/getItem")
	public NoticeItem getNoticeItem(@RequestParam(name = "noticeId") int noticeId) {
		return mainSO.selectOne(noticeId);
	}
}