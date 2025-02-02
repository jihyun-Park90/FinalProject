
package com.project.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.google.zxing.WriterException;
import com.project.model.*;
import com.project.model.response.LoginResponse;
import com.project.service.*;

import jakarta.servlet.http.HttpSession;

import org.springframework.ui.Model;

@Controller
@RequestMapping("/")
public class MainController {
	@Autowired
	MainSO mainSO;
	@Autowired
	QrCodeSO qrSO;
	@Autowired
	CourseDAO courseDAO;
	@Autowired
	EmailSO emailSO;
	@Autowired
	ImageUploadSO uploadSO;

	private String viewPath;

	@GetMapping("/goCourseHome")
	public String goCourseHome(@RequestParam(required = true, name = "courseId") int courseId, HttpSession session) {
		session.setAttribute("currentId", courseId);
		session.setAttribute("courseName", courseDAO.getCourseName(courseId));

		return "redirect:/home";
	}

	@GetMapping("/")
	public String getMain(Model model, @RequestParam(required = false, defaultValue = "1", name = "page") int page,
			HttpSession session) {
		int memberId, memberRole;
		try {
			memberId = this.getMemberId(session);
			memberRole = this.getMemberRole(session);

			if (memberRole == 1) {
				// 로그인한 회원이 학생일 때
				model.addAttribute("course", mainSO.selectByMemberId(memberId, page));
				model.addAttribute("notice", mainSO.selectList(1, 3));
				model.addAttribute("size", mainSO.getSizeByMemberId(memberId));
			} else {
				// 로그인한 회원이 강사일 때
				model.addAttribute("course", mainSO.selectByInstructorId(memberId, page));
				model.addAttribute("notice", mainSO.selectList(1, 3));
				model.addAttribute("size", mainSO.getSizeByInstructorId(memberId));
			}
			model.addAttribute("page", page);
			model.addAttribute("menu", "main");
		} catch (Exception e) {
			e.printStackTrace();
			return "redirect:/login";
		}
		return "main/index";
	}

	@GetMapping("/checkin")
	public String getCheckin(Model model, HttpSession session) throws UnsupportedEncodingException {
		int memberId, studentId, courseId, memberRole;
		try {
			memberId = this.getMemberId(session);
			memberRole = this.getMemberRole(session);
			if (memberRole == 1) {
				// 로그인한 회원이 학생일 때
				studentId = mainSO.checkCourseForStudentId(memberId);
				model.addAttribute("info", mainSO.getInfoByStudentId(studentId));
				model.addAttribute("stats", mainSO.getStats(studentId));
				model.addAttribute("time", mainSO.getTimetable(studentId));
				viewPath = "main/checkin";
			} else {
				// 로그인한 회원이 강사일 때
				courseId = mainSO.checkCourseForCourseId(memberId);
				try {
					model.addAttribute("info", mainSO.getInfoByCourseId(courseId));
				} catch (Exception e) {
					System.out.println("정보 오류");
				}
				try {
					model.addAttribute("stats", mainSO.getStatsByCourseId(courseId));
				} catch (Exception e) {
					System.out.println("통계 오류");
				}
				viewPath = "main/checkin_i";
			}
			model.addAttribute("menu", "checkin");
			return viewPath;
		} catch (Exception e) {
			return this.redirectErrorPage("금일 해당하는 강의가 없습니다. 관리자에게 문의하세요", "courseNotFound");
		}
	}

	@ResponseBody
	@GetMapping("/api/checkin/getQRImage")
	public ResponseEntity<byte[]> generateQRCodeImage(@RequestParam(name = "id") int courseId)
			throws WriterException, IOException {
		CourseItem qrData = mainSO.getQrCode(courseId, new CourseItem());
		return qrSO.generateQRCodeImage(qrData.getQrCode());
	}

	@ResponseBody
	@GetMapping("/api/checkin/createQR")
	public MessageItem createQR(Model model, HttpSession session) {
		int memberId, courseId;
		MessageItem response = new MessageItem();
		try {
			memberId = this.getMemberId(session);
			courseId = mainSO.checkCourseForCourseId(memberId);

			CourseItem courseItem = mainSO.getQrCode(courseId, new CourseItem());
			if (courseItem.getQrCode() == null) {
				try {
					String encryptedText = qrSO.getEncryptedText(courseItem.toString());
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
	}

	@ResponseBody
	@GetMapping("/api/checkin/update")
	public MessageItem updateTimetable(@RequestParam(required = true, name = "keyword") String keyword,
			@RequestParam(required = true, name = "code") String code, HttpSession session) {
		int memberId, studentId;
		MessageItem response = new MessageItem();

		try {
			memberId = this.getMemberId(session);
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
			@RequestParam(required = false, defaultValue = "1", name = "page") int page, HttpSession session) {
		model.addAttribute("list", mainSO.selectByDates(page, this.getMemberId(session)));
		model.addAttribute("size", mainSO.getSizeByDates(this.getMemberId(session)));
		model.addAttribute("page", page);
		model.addAttribute("menu", "register");
		return "main/register";
	}

	@RequestMapping("/register/search")
	public String searchCourse(Model model, @RequestParam(required = false, defaultValue = "1", name = "page") int page,
			@RequestParam(required = true, name = "keyword") String keyword, HttpSession session) {
		model.addAttribute("list", mainSO.selectByDates(page, this.getMemberId(session), keyword));
		model.addAttribute("size", mainSO.getSizeByDates(this.getMemberId(session), keyword));
		model.addAttribute("page", page);
		model.addAttribute("menu", "register");
		model.addAttribute("keyword", keyword);
		return "main/register_search";
	}

	@ResponseBody
	@RequestMapping("/api/course/register")
	public MessageItem register(@RequestParam(name = "courseId") int courseId, HttpSession session) {
		int memberId;
		MessageItem messageItem = new MessageItem();
		memberId = this.getMemberId(session);
		if (mainSO.checkCourseConflicts(memberId, courseId)) {
			messageItem.setRes(false);
			messageItem.setMsg("동일한 시간대에 수강 중인 강의가 있거나, 이미 수강 중인 강의입니다.");
		} else if (mainSO.checkAlreadyRegistered(memberId, courseId)) {
			messageItem.setRes(false);
			messageItem.setMsg("이미 수강 신청을 요청한 강의입니다.");
		} else {
			try {
				messageItem.setRes(mainSO.register(memberId, courseId));
			} catch (Exception e) {
				messageItem.setMsg("수강 신청 요청이 처리되지 않았습니다.");
			}
		}
		return messageItem;
	}

	@GetMapping("/notice")
	public String getNotice(Model model, @RequestParam(required = false, defaultValue = "1", name = "page") int page,
			HttpSession session) {
		int memberRole;
		try {
			memberRole = this.getMemberRole(session);
			if (memberRole == 1) {
				// 학생으로 로그인했을 때
				model.addAttribute("list", mainSO.selectList(page));
				model.addAttribute("size", mainSO.getSize());
				model.addAttribute("page", page);
			} else {
				// 강사 또는 관리자로 로그인했을 때
				model.addAttribute("list", mainSO.selectAll(page));
				model.addAttribute("size", mainSO.getTotalSize());
				model.addAttribute("page", page);
			}
			model.addAttribute("menu", "notice");
		} catch (Exception e) {
			return "redirect:/login";
		}

		return "main/notice";
	}

	@RequestMapping("/notice/search")
	public String searchNotice(Model model, @RequestParam(required = false, defaultValue = "1", name = "page") int page,
			@RequestParam(required = true, name = "keyword") String keyword) {
		model.addAttribute("list", mainSO.selectList(page, keyword));
		model.addAttribute("size", mainSO.getSize(keyword));
		model.addAttribute("page", page);
		model.addAttribute("menu", "notice");
		model.addAttribute("keyword", keyword);
		return "main/notice_search";
	}

	@ResponseBody
	@GetMapping("/api/notice/getItem")
	public NoticeItem getNoticeItem(@RequestParam(name = "noticeId") int noticeId) {
		NoticeItem noticeItem = mainSO.selectOne(noticeId);
		List<FileItem> files;
		try {
			files = uploadSO.getFiles(noticeItem.getAttachments());
			noticeItem.setAttms(files);
			noticeItem.setAttachments("");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return noticeItem;
	}

	@RequestMapping("/main/error")
	public String getErrorPage(@RequestParam("msg") String msg, @RequestParam("redirect") String redirect,
			Model model) {
		model.addAttribute("msg", msg);
		model.addAttribute("redirect", redirect);
		return "admin/error";
	}

	private String redirectErrorPage(String errorMessage, String redirectKeyword) throws UnsupportedEncodingException {
		return "redirect:/main/error?msg=" + URLEncoder.encode(errorMessage, "UTF-8") + ".&redirect=" + redirectKeyword;
	}

	private int getMemberId(HttpSession session) {
		try {
			LoginResponse auth = (LoginResponse) session.getAttribute("auth");
			return auth.getMember_id();
		} catch (Exception e) {
			return -1;
		}
	}

	private int getMemberRole(HttpSession session) {
		try {
			LoginResponse auth = (LoginResponse) session.getAttribute("auth");
			return auth.getM_role();
		} catch (Exception e) {
			return -1;
		}
	}
}