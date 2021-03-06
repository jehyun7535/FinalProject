package kr.or.ddit.project.web;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import kr.or.ddit.common.model.PageVo;
import kr.or.ddit.common.model.SearchVo;
import kr.or.ddit.contract.model.MeetingVo;
import kr.or.ddit.contract.service.ContractService;
import kr.or.ddit.evaluation.model.EvaluationVo;
import kr.or.ddit.note.service.NoteService;
import kr.or.ddit.project.model.PAttendVo;
import kr.or.ddit.project.model.ProjectVo;
import kr.or.ddit.project.service.ProjectService;
import kr.or.ddit.user.model.MessageVo;
import kr.or.ddit.user.model.UserVo;
import kr.or.ddit.user.service.MessageService;
import kr.or.ddit.user.service.UserService;
import kr.or.ddit.view.Coolsms;

@Controller
@RequestMapping("project")
public class ProjectController {

	private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);

	@Resource(name = "NoteServiceImpl")
	private NoteService noteService;
	
	@Resource(name = "projectService")
	private ProjectService projectService;

	@Resource(name = "contractService")
	private ContractService contractService;
	
	@Resource(name = "messageService")
	private MessageService messageService;

	@Resource(name = "userService")
	private UserService userService;

	@RequestMapping("selectProject")
	public String project(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "5") int pageSize,
			Model model, HttpSession session) {
		model.addAttribute("alarmList",
				messageService.alarmMessage(((UserVo) session.getAttribute("S_USER")).getUser_id()));
		if (((UserVo) session.getAttribute("S_USER")).getPurpose().equals("C")) {
			model.addAttribute("pList",
					projectService.ingProjectListC(((UserVo) session.getAttribute("S_USER")).getUser_id()));
		} else if (((UserVo) session.getAttribute("S_USER")).getPurpose().equals("P")) {
			model.addAttribute("pList",
					projectService.ingProjectListP(((UserVo) session.getAttribute("S_USER")).getUser_id()));
		}
		PageVo pageVo = new PageVo(page, pageSize);

		model.addAllAttributes(projectService.PagingProject(pageVo));

		return "t/project/projectList";
	}

	@RequestMapping(path = "insertProject", method = RequestMethod.GET)
	public String insertproject(Model model, UserVo userVo, HttpSession session) {
		model.addAttribute("alarmList",
				messageService.alarmMessage(((UserVo) session.getAttribute("S_USER")).getUser_id()));
		
		if (((UserVo) session.getAttribute("S_USER")).getPurpose().equals("C")) {
		} else if (((UserVo) session.getAttribute("S_USER")).getPurpose().equals("P")) {
			model.addAttribute("pList",
					projectService.ingProjectListP(((UserVo) session.getAttribute("S_USER")).getUser_id()));
		}
		return "t/project/insertProject";
	}

	@RequestMapping(path = "insertProject", method = RequestMethod.POST)
	public String insertproject(Model model, ProjectVo projectVo, String ps_no) {
		String str = ps_no;
		String[] array = str.split(",");
		int insertCnt = projectService.insertProject(projectVo);
		for (int i = 0; i < array.length; i++) {
			String ps = array[i];
			projectService.insertPskill(ps);
		}
		if (insertCnt == 1) {
			return "redirect:/project/beforeProjectList";
		}
		return "t/project/insertProject";
	}

	@RequestMapping(path="viewProject", method=RequestMethod.GET)
	public String viewproject(Model model, int p_code, HttpSession session) {      // () ?????? ????????? ???????????? ??????
		model.addAttribute("alarmList", messageService.alarmMessage(((UserVo) session.getAttribute("S_USER")).getUser_id()));
		if(((UserVo) session.getAttribute("S_USER")).getPurpose().equals("C")) {
			model.addAttribute("pList", projectService.ingProjectListC(((UserVo) session.getAttribute("S_USER")).getUser_id()));
		} else if(((UserVo) session.getAttribute("S_USER")).getPurpose().equals("P")) {
			model.addAttribute("pList", projectService.ingProjectListP(((UserVo) session.getAttribute("S_USER")).getUser_id()));
		}
		String user_id = ((UserVo) session.getAttribute("S_USER")).getUser_id();
		
		PAttendVo pattend = new PAttendVo();
		pattend.setP_code(p_code);
		pattend.setUser_id(user_id);
  
		model.addAttribute("project", projectService.viewProject(p_code));
		model.addAttribute("applicant", projectService.applicantCnt(p_code));
		model.addAttribute("pstate", projectService.selectPstate(pattend));
		model.addAttribute("pskill", projectService.listPskill(p_code));
  
		return "t/project/viewProject";
	}

	@RequestMapping(path = "insertLike", method = RequestMethod.POST)
	public String insertlike(Model model, PAttendVo pattendVo, RedirectAttributes ra) {

//		logger.debug("pattend : {}", pattendVo);

		int insertCnt = projectService.insertLike(pattendVo);
//		logger.debug("cnt : {}", insertCnt);
		if (insertCnt == 1) {
			ra.addFlashAttribute("msg", "???????????????????????????.");
			return "redirect:/project/viewProject?p_code=" + pattendVo.getP_code();
		}
		return "redirect:/project/viewProject?p_code=" + pattendVo.getP_code();
	}

	@RequestMapping(path = "deleteLike", method = RequestMethod.POST)
	public String deletelike(Model model, PAttendVo pattendVo, RedirectAttributes ra) {

		logger.debug("delete??????vo : {}", pattendVo);
		int deleteCnt = projectService.deletelike(pattendVo);

		if (deleteCnt == 1) {
			ra.addFlashAttribute("msg", "?????????????????????.");
			return "redirect:/project/viewProject?p_code=" + pattendVo.getP_code();
		}

		return "redirect:/project/viewProject?p_code=" + pattendVo.getP_code();
	}

	@RequestMapping(path = "insertApply", method = RequestMethod.POST)
	public String insertapply(Model model, PAttendVo pattendVo, RedirectAttributes ra) {

		logger.debug("pattend : {}", pattendVo);

		int insertCnt = projectService.insertApply(pattendVo);
		logger.debug("cnt : {}", insertCnt);
		if (insertCnt == 1) {
			ra.addFlashAttribute("msg", "????????????.");
			return "redirect:/project/viewProject?p_code=" + pattendVo.getP_code();
		}
		return "t/project/projectList";
	}

	@RequestMapping(path = "updateApply", method = RequestMethod.POST)
	public String updateapply(Model model, PAttendVo pattendVo, RedirectAttributes ra) {

		logger.debug("pattend : {}", pattendVo);

		int updateCnt = projectService.updateApply(pattendVo);
		logger.debug("cnt : {}", updateCnt);
		if (updateCnt == 1) {
			ra.addFlashAttribute("msg", "????????????.");
			return "redirect:/project/viewProject?p_code=" + pattendVo.getP_code();
		}
		return "project/updateapply";
	}

	public class SessionException extends Exception {
		public SessionException() {}
		public SessionException(String msg) {
			super(msg);
		}
	}
	
	
	/** ???????????? ?????? ????????????(?????????????????? -> ?????? -> ????????? -> ????????????) 
	 *  1. ???????????? ??????(??????????????????+??????????????????+??????) 
	 *  2. ???????????? ??????(????????????, ???????????????, ???????????????)
	 *  3. ???????????????(???????????? ????????? ?????? ??????????????? ???) 
	 *    - ?????? : C(create,insert) R(read, select) U(update) D(delete) 
	 *      - ????????????????????? : [ ????????????????????? + ?????????????????? + ???????????? + ????????????(?????????) ]??? ????????? ???????????? ?????? 
	 *    - ????????? : 2??? ?????? ?????? ?????? ?
	 *      - ????????????(100???)??? ????????? ????????? ????????? -> ????????? ????????????????????? ?????? ????????? ???????????? ?????? ?????? ?????? ??????????????? ?????? 
	 *      
	 *     String[] data = null;
			   for (String s : data) {
			   		try {
						?????????
					}catch(SQLException e) {
						
					}
				} 
	 *      
	 *  4. ??????????????? ?????? ?????? ??????
	 *  5. view ????????? ??????    
	 * @throws SQLException 
	 *        
	 */
	/*
	@RequestMapping("searchProject")
	public String searchProject(Model model, @RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "5") int pageSize, String sT, String kW, RedirectAttributes ra,
			HttpSession session) throws SessionException, SQLException {
		
		// 1. ???????????? ??????/??????  
		if (kW.equals("") || sT.equals("")) {
			ra.addAttribute("page", page);
			ra.addAttribute("pageSize", pageSize);
			return "redirect:/project/selectProject";
		}
		
		UserVo userVo = (UserVo) session.getAttribute("S_USER");
//		if(userVo==null) {
//			return "redirect:??????????????????URL";
//			//throw new SessionException("????????? ???????????? ????????????.");
//		}

		String userId = userVo.getUser_id();
		String purpose = userVo.getPurpose();
		PageVo pageVo = new PageVo(page, pageSize, kW);
		
		//-----------------------------------------------------------------------------------------
		// ?????? ?????? ????????? ????????? 
		List<MessageVo> alarmMessage = messageService.alarmMessage(userId);
		
		// ???????????? ???????????? ?????????  
		Map<String,Object> map = new HashMap<>();
		map.put("userId", userId);
		map.put("purpose", purpose);
		map.put("pageInfo", pageVo);
		
		logger.debug("????????? ???????????? ???????????? : {} ", map);
		List<ProjectVo> ingProjectList = projectService.ingProjectList(map);
		
		// ???????????? ?????? ?????? 
		projectService.getProjectList(map);
		List<ProjectVo> projectVoList = (List<ProjectVo>) map.get("projectList");
		int cnt = (int) map.get("cnt");
		// -------------------------------------------------------------------------------------------
		
		// ??????????????? ?????? ?????? ?????? 
		model.addAttribute("alarmList", alarmMessage);
		model.addAttribute("pList", ingProjectList);
		model.addAttribute("pagination", (int) Math.ceil((double) cnt / pageSize));
		model.addAttribute("pageVo", pageVo);
		model.addAttribute("sT", sT);
		model.addAttribute("kW", kW);
		
		// view ????????? ?????? 
		return "t/project/projectList";
		// --------------------------------------------------------------------------------
	}
	*/
	
	
	@RequestMapping("searchProject")
	public String searchProject(Model model, @RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "5") int pageSize, String sT, String kW, RedirectAttributes ra,
			HttpSession session) {
		model.addAttribute("alarmList",
				messageService.alarmMessage(((UserVo) session.getAttribute("S_USER")).getUser_id()));
		if (((UserVo) session.getAttribute("S_USER")).getPurpose().equals("C")) {
			model.addAttribute("pList",
					projectService.ingProjectListC(((UserVo) session.getAttribute("S_USER")).getUser_id()));
		} else if (((UserVo) session.getAttribute("S_USER")).getPurpose().equals("P")) {
			model.addAttribute("pList",
					projectService.ingProjectListP(((UserVo) session.getAttribute("S_USER")).getUser_id()));
		}
		logger.debug("kW : {} , sT : {} ", kW, sT);
		Map<String, Object> map = null;

		PageVo vo = new PageVo(page, pageSize, kW);
		if (kW.equals("") || sT.equals("")) {
			ra.addAttribute("page", page);
			ra.addAttribute("pageSize", pageSize);
			return "redirect:/project/selectProject";
		} else {
			if (sT.equals("i")) {
				map = projectService.searchProjectid(vo);
			} else if (sT.equals("p")) {
				map = projectService.searchProjectnm(vo);
			}
			int cnt = (int) map.get("cnt");
			model.addAttribute("projectList", map.get("projectList"));
			model.addAttribute("pagination", (int) Math.ceil((double) cnt / pageSize));
			model.addAttribute("pageVo", vo);
			model.addAttribute("sT", sT);
			model.addAttribute("kW", kW);
			return "t/project/projectList";
		}
	}

	// ????????? ????????? ?????? ?????? ?????????
	@RequestMapping("selectLikeList")
	public String selectlikelist(Model model, ProjectVo projectvo, HttpSession session) {
		model.addAttribute("alarmList",
				messageService.alarmMessage(((UserVo) session.getAttribute("S_USER")).getUser_id()));
		if (((UserVo) session.getAttribute("S_USER")).getPurpose().equals("C")) {
			model.addAttribute("pList",
					projectService.ingProjectListC(((UserVo) session.getAttribute("S_USER")).getUser_id()));
		} else if (((UserVo) session.getAttribute("S_USER")).getPurpose().equals("P")) {
			model.addAttribute("pList",
					projectService.ingProjectListP(((UserVo) session.getAttribute("S_USER")).getUser_id()));
		}

		String user_id = ((UserVo) session.getAttribute("S_USER")).getUser_id();

		model.addAttribute("likeList", projectService.selectLike(user_id));

		return "t/project/selectLikeList";
	}

	// ????????? ????????? ?????? ?????? ????????? ????????? ???????????? ??????
	@RequestMapping(path = "deleteLikeList", method = RequestMethod.POST)
	public String deletelikeList(Model model, PAttendVo pattendVo, RedirectAttributes ra) {

		int deleteCnt = projectService.deletelike(pattendVo);

		if (deleteCnt == 1) {
			ra.addFlashAttribute("msg", "?????????????????????.");
			return "redirect:/project/selectLikeList";
		}
		return "t/project/selectLikeList";
	}

	// ???????????? ?????????
	@RequestMapping("applyList")
	public String applyList(Model model, ProjectVo projectvo, HttpSession session) {
		String user_id = ((UserVo) session.getAttribute("S_USER")).getUser_id();
		model.addAttribute("alarmList", messageService.alarmMessage(user_id));
		if (((UserVo) session.getAttribute("S_USER")).getPurpose().equals("C")) {
			model.addAttribute("pList",
					projectService.ingProjectListC(((UserVo) session.getAttribute("S_USER")).getUser_id()));
		} else if (((UserVo) session.getAttribute("S_USER")).getPurpose().equals("P")) {
			model.addAttribute("pList",
					projectService.ingProjectListP(((UserVo) session.getAttribute("S_USER")).getUser_id()));
		}

		model.addAttribute("applyList", projectService.applyList(user_id));

		return "t/project/applyList";
	}

	@RequestMapping(path = "deleteApplyList", method = RequestMethod.POST)
	public String deletapplyList(Model model, PAttendVo pattendVo, RedirectAttributes ra) {
		int deleteCnt = projectService.deletelike(pattendVo);

		if (deleteCnt == 1) {
			ra.addFlashAttribute("msg", "?????????????????????.");
			return "redirect:/project/applyList";
		}
		return "project/applyList";
	}

	@RequestMapping("ingProjectList")
	public String ingprojectlist(Model model, ProjectVo projectvo, HttpSession session) {

		/** ?????? ???????????? ?????? - ?????? ???????????? ????????? ??????, ?????? */
		String user_id = ((UserVo) session.getAttribute("S_USER")).getUser_id();
		String purpose = ((UserVo) session.getAttribute("S_USER")).getPurpose();
		
		
		/** ?????? : Validation */ 

		/** ????????? ?????? */ 
		model.addAttribute("alarmList", messageService.alarmMessage(user_id));
		if (((UserVo) session.getAttribute("S_USER")).getPurpose().equals("C")) {
			model.addAttribute("pList",
					projectService.ingProjectListC(((UserVo) session.getAttribute("S_USER")).getUser_id()));
		} else if (((UserVo) session.getAttribute("S_USER")).getPurpose().equals("P")) {
			model.addAttribute("pList",
					projectService.ingProjectListP(((UserVo) session.getAttribute("S_USER")).getUser_id()));
		}
		logger.debug("?????? : {}", purpose);
		if (purpose.equals("C")) {
			model.addAttribute("ingProjectList", projectService.ingProjectListC(user_id));
			logger.debug("?????? C : {}", purpose);
		} else {
			model.addAttribute("ingProjectList", projectService.ingProjectListP(user_id));
			logger.debug("?????? P : {}", purpose);
		}
		
		/** client ????????? ?????? ?????? */ 
		
		
		/** ??????(view) ?????? */ 
		return "t/project/ingProjectList";
	}

	@RequestMapping("finishProjectList")
	public String finishprojectlist(Model model, ProjectVo projectvo, HttpSession session) {

		String user_id = ((UserVo) session.getAttribute("S_USER")).getUser_id();
		model.addAttribute("alarmList", messageService.alarmMessage(user_id));
		if (((UserVo) session.getAttribute("S_USER")).getPurpose().equals("C")) {
			model.addAttribute("pList",
					projectService.ingProjectListC(((UserVo) session.getAttribute("S_USER")).getUser_id()));
		} else if (((UserVo) session.getAttribute("S_USER")).getPurpose().equals("P")) {
			model.addAttribute("pList",
					projectService.ingProjectListP(((UserVo) session.getAttribute("S_USER")).getUser_id()));
		}
		String purpose = ((UserVo) session.getAttribute("S_USER")).getPurpose();

		if (purpose.equals("C")) {
			model.addAttribute("finishProjectList", projectService.finishProjectListC(user_id));
		} else {
			model.addAttribute("finishProjectList", projectService.finishProjectListP(user_id));
		}
		return "t/project/finishProjectList";
	}

	@RequestMapping("beforeProjectList")
	public String beforeprojectlist(Model model, ProjectVo projectvo, HttpSession session) {

		String user_id = ((UserVo) session.getAttribute("S_USER")).getUser_id();
		model.addAttribute("alarmList", messageService.alarmMessage(user_id));
		if (((UserVo) session.getAttribute("S_USER")).getPurpose().equals("C")) {
			model.addAttribute("pList",
					projectService.ingProjectListC(((UserVo) session.getAttribute("S_USER")).getUser_id()));
		} else if (((UserVo) session.getAttribute("S_USER")).getPurpose().equals("P")) {
			model.addAttribute("pList",
					projectService.ingProjectListP(((UserVo) session.getAttribute("S_USER")).getUser_id()));
		}
		model.addAttribute("beforeprojectList", projectService.beforeProject(user_id));

		return "t/project/beforeProjectList";
	}

	// ??????????????? --> ???????????? ?????????????????? ?????????
	@RequestMapping("requestedapplylist")
	public String requestedapplylist(Model model, ProjectVo projectVo, HttpSession session) {
		model.addAttribute("alarmList",
				messageService.alarmMessage(((UserVo) session.getAttribute("S_USER")).getUser_id()));
		if (((UserVo) session.getAttribute("S_USER")).getPurpose().equals("C")) {
			model.addAttribute("pList",
					projectService.ingProjectListC(((UserVo) session.getAttribute("S_USER")).getUser_id()));
		} else if (((UserVo) session.getAttribute("S_USER")).getPurpose().equals("P")) {
			model.addAttribute("pList",
					projectService.ingProjectListP(((UserVo) session.getAttribute("S_USER")).getUser_id()));
		}
		logger.debug("?????????????{}");
		String user_id = ((UserVo) session.getAttribute("S_USER")).getUser_id();
		 
		model.addAttribute("reqapplyList", projectService.requestedApply(user_id));
		return "t/project/requestedApplyList";
	}
	
	/**
	 * ??????
	 */
	// ????????? ?????????
	@RequestMapping("selectUserProject")
	public String selectUserProject(@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "5") int pageSize, String user_id, Model model, HttpSession session) {
		model.addAttribute("alarmList",
				messageService.alarmMessage(((UserVo) session.getAttribute("S_USER")).getUser_id()));
		if (((UserVo) session.getAttribute("S_USER")).getPurpose().equals("C")) {
			model.addAttribute("pList",
					projectService.ingProjectListC(((UserVo) session.getAttribute("S_USER")).getUser_id()));
		} else if (((UserVo) session.getAttribute("S_USER")).getPurpose().equals("P")) {
			model.addAttribute("pList",
					projectService.ingProjectListP(((UserVo) session.getAttribute("S_USER")).getUser_id()));
		}
		ProjectVo pageVo = new ProjectVo(page, pageSize, user_id);
		model.addAllAttributes(projectService.selectUserProject(pageVo));
		return "t/project/recruitment";
	}

	// ????????? ??????
	@RequestMapping("viewPattendUser")
	public String viewPattendUser(@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "10") int pageSize, int p_code , String user_id, Model model,
			RedirectAttributes ra, HttpSession session) {
		model.addAttribute("alarmList",
				messageService.alarmMessage(((UserVo) session.getAttribute("S_USER")).getUser_id()));
		if (((UserVo) session.getAttribute("S_USER")).getPurpose().equals("C")) {
			model.addAttribute("pList",
					projectService.ingProjectListC(((UserVo) session.getAttribute("S_USER")).getUser_id()));
		} else if (((UserVo) session.getAttribute("S_USER")).getPurpose().equals("P")) {
			model.addAttribute("pList",
					projectService.ingProjectListP(((UserVo) session.getAttribute("S_USER")).getUser_id()));
		}
		PageVo pageVo = new PageVo(page, pageSize);
		pageVo.setP_code(p_code);
		model.addAllAttributes(projectService.viewPattendUser(pageVo));

		List<UserVo> userList = (List<UserVo>) projectService.viewPattendUser(pageVo).get("userList");
//		Map<String, Object> map = projectService.viewPattendUser(pageVo);
		if (userList.isEmpty()) {
			ra.addFlashAttribute("msg", "???????????? ????????????.");
			return "redirect:/project/selectUserProject?user_id=" + user_id;
		}

		return "t/project/recruitmentUser";

	}

	// ???????????? ?????? - ?????? 04
	@RequestMapping(path = "updateProjectState", method = RequestMethod.GET)
	public String updateProjectState(Model model, ProjectVo projectVo, RedirectAttributes ra) {
		int updateCnt = projectService.updateProjectState(projectVo);
		if (updateCnt == 1) {
			ra.addFlashAttribute("msg", "??????????????? ?????????????????????.");
			return "redirect:/project/selectUserProject?user_id=" + projectVo.getUser_id();
		}
		return "t/project/selectUserProject";
	}

	// ????????? ??????
	@RequestMapping(path = "projectOk", method = RequestMethod.POST)
	public String projectOk(HttpServletRequest request, Model model, PAttendVo pattendVo, String user_nm, RedirectAttributes ra, HttpSession session, MeetingVo meeting) {
		
		String api_key = "NCSV6CKUIUNPUQKX";
	    String api_secret = "LWQOJ358XER2VBMAYJJRD3NJWBJJRUFU";
//	    Coolsms coolsms = new Coolsms(api_key, api_secret);
	    
//		HashMap<String, String> set = new HashMap<String, String>();
//		
//		set.put("to", projectService.sendPhone(pattendVo.getUser_id())); // ????????????
//        set.put("from", ((UserVo)session.getAttribute("S_USER")).getPhone()); // ????????????, jsp?????? ????????? ??????????????? ?????? map??? ????????????.
//        set.put("text", projectService.sendTitle(pattendVo.getP_code()) + " ?????????????????????."); // ????????????, jsp?????? ????????? ??????????????? ?????? map??? ????????????.
//        set.put("type", "sms"); // ?????? ??????
//        
//	    System.out.println(set);

//	    JSONObject result = coolsms.send(set); // ?????????&??????????????????
		
		
		String client = ((UserVo)session.getAttribute("S_USER")).getUser_id();
		logger.debug(client);
		int updateCnt = projectService.updateProjectOk(new PAttendVo(pattendVo.getP_code(), pattendVo.getUser_id()));
		if (updateCnt == 1) {
			meeting.setPtn_id(pattendVo.getUser_id());
			contractService.insertContract(meeting);
			logger.debug(" ?????? : {} ", meeting);
			
			ra.addFlashAttribute("msg", user_nm + "??? ?????? ???????????????.");
			projectService.projectStateMsg(new MessageVo(
					"??????????????? ??????????????? ?????? ???????????????. <br> ??????????????? ???????????? ???????????????  <div class='font-icon-list p-2 border mx-1 mb-2'>"
							+ "<a href='http://localhost:80/project/viewProject?p_code=" + pattendVo.getP_code()
							+ "'>???????????? ???????????? ??????</a></div>",
					pattendVo.getUser_id(), client));
			
//			if ((boolean)result.get("status") == true) {
//			      // ????????? ????????? ?????? ??? ???????????? ??????
//			      System.out.println("??????");
//			      System.out.println(result.get("group_id")); // ???????????????
//			      
//			      System.out.println(result.get("result_code")); // ????????????
//			      System.out.println(result.get("result_message")); // ?????? ?????????
//			      System.out.println(result.get("success_count")); // ??????????????????
//			      System.out.println(result.get("error_count")); // ????????? ????????? ????????? ????????? ???
//			}else {
//			      // ????????? ????????? ??????
//			      System.out.println("??????");
//			      System.out.println(result.get("code")); // REST API ????????????
//			      System.out.println(result.get("message")); // ???????????????
//			}
		}
		return "redirect:/project/viewPattendUser?p_code=" + pattendVo.getP_code();
	}

	// ????????? ??????
	@RequestMapping(path = "projectNo", method = RequestMethod.POST)
	public String projectNo(Model model, PAttendVo pattendVo, HttpSession session, RedirectAttributes ra) {
		
		String client = ((UserVo)session.getAttribute("S_USER")).getUser_id();
		
		int updateCnt = projectService.updateProjectNo(new PAttendVo(pattendVo.getP_code(), pattendVo.getUser_id()));
		if (updateCnt == 1) {
			ra.addFlashAttribute("msg", "?????? ???????????????.");
			projectService.projectStateMsg(
					new MessageVo("??????????????? ????????? ??????????????? ?????????????????????.<br> ????????? ?????? ?????? ??????????????????.", pattendVo.getUser_id(), client));
		}
		return "redirect:/project/viewPattendUser?p_code=" + pattendVo.getP_code();
	}

	// ?????? ?????? - ??????
	@RequestMapping(path = "searchFilterPrice", method = RequestMethod.GET)
	public String searchFilterPrice(Model model, @RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "5") int pageSize, @RequestParam(defaultValue = "0") String st,
			@RequestParam(defaultValue = "") String end, String state, RedirectAttributes ra, HttpSession session) {
		model.addAttribute("alarmList",
				messageService.alarmMessage(((UserVo) session.getAttribute("S_USER")).getUser_id()));
		if (((UserVo) session.getAttribute("S_USER")).getPurpose().equals("C")) {
			model.addAttribute("pList",
					projectService.ingProjectListC(((UserVo) session.getAttribute("S_USER")).getUser_id()));
		} else if (((UserVo) session.getAttribute("S_USER")).getPurpose().equals("P")) {
			model.addAttribute("pList",
					projectService.ingProjectListP(((UserVo) session.getAttribute("S_USER")).getUser_id()));
		}
		if(st.equals("0") && end.equals("") ) {
			return "redirect:/project/selectProject";
		}
		PageVo vo = new PageVo(page, pageSize);
		if (!st.equals("0") && end.equals("") ) {
			logger.debug("1");
			vo.setStart(st+ "0000");
			model.addAttribute("state", "price");
			model.addAllAttributes(projectService.searchFilterPrice(vo));
//			return "t/project/projectSearch";
		} else if (!st.equals("") && !end.equals("")) {
			vo.setStart(st+"0000");
			vo.setEnd(end+"0000");
			logger.debug("2");
			model.addAttribute("state", "price");
			model.addAllAttributes(projectService.searchFilterPrice(vo));
//			return "t/project/projectSearch";
		} else if ((st.equals("0") && !end.equals(""))) {
			vo.setStart(st+"0000");
			vo.setEnd(end+"0000");
			logger.debug("3");
			model.addAttribute("state", "price");
			model.addAllAttributes(projectService.searchFilterPrice(vo));
		}
//		model.addAttribute("st", st.substring(0, st.length() - 4));
//		model.addAttribute("end", end.substring(0, end.length() - 4));
		logger.debug("st : {} , end : {} ", st, end);
		return "t/project/projectSearch";
//		}
	}

	// ?????? ?????? - ??????
	@RequestMapping(path = "searchFilterPreiod", method = RequestMethod.GET)
	public String searchFilterPreiod(Model model, @RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "5") int pageSize, @RequestParam(defaultValue = "0") String st,
			@RequestParam(defaultValue = "") String end, String state, RedirectAttributes ra, HttpSession session) {

		logger.debug("st : {} , end : {} ", st, end);

		model.addAttribute("alarmList",
				messageService.alarmMessage(((UserVo) session.getAttribute("S_USER")).getUser_id()));
		if (((UserVo) session.getAttribute("S_USER")).getPurpose().equals("C")) {
			model.addAttribute("pList",
					projectService.ingProjectListC(((UserVo) session.getAttribute("S_USER")).getUser_id()));
		} else if (((UserVo) session.getAttribute("S_USER")).getPurpose().equals("P")) {
			model.addAttribute("pList",
					projectService.ingProjectListP(((UserVo) session.getAttribute("S_USER")).getUser_id()));
		}
		PageVo vo = new PageVo(page, pageSize);
		if (st.equals("") && end.equals("")) {
			logger.debug("1");
			vo.setStart(st);
			model.addAttribute("state", "preiod");
			model.addAllAttributes(projectService.searchFilterPreiod(vo));
//			return "t/project/projectSearch";
		} else if (!st.equals("") && !end.equals("")) {
			vo.setStart(st);
			vo.setEnd(end);
			logger.debug("2");
			model.addAttribute("state", "preiod");
			model.addAllAttributes(projectService.searchFilterPreiod(vo));
//			return "t/project/projectSearch";
		} else if ((!st.equals("") && end.equals("") || st.equals("0") && !end.equals(""))) {
			vo.setStart(st);
			vo.setEnd(end);
			logger.debug("3");
			model.addAttribute("state", "preiod");
			model.addAllAttributes(projectService.searchFilterPreiod(vo));
		}
		return "t/project/projectSearch";
	}

	// ?????? ?????? - ?????????
	@RequestMapping("searchFilter")
	public String searchFilterPaging(@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "5") int pageSize, @RequestParam(defaultValue = "0") String st,
			@RequestParam(defaultValue = "") String end, String p_filed, String state) {

		logger.debug("st : {} , end : {} ", st, end);
		logger.debug("state : {} ", state);
		// ?????? - ?????? ??????
		if (state.equals("price")) {
			logger.debug("?????? 1");
			return "redirect:/project/searchFilterPrice?page=" + page + "&pageSize=" + pageSize + "&st=" + st + "&end="
					+ end + "&state=" + state;
		} // ???????????? ????????? ???
		else if (state.equals("filed")) {
			logger.debug("filed ?????? : {} "+ p_filed);
			return "redirect:/project/searchFilterpfileds?page=" + page + "&pageSize=" + pageSize + "&p_filed=" + p_filed
					+ "&state=" + state;
		} // ?????? - ?????? ??????
		else {
			return "redirect:/project/searchFilterPreiod?page=" + page + "&pageSize=" + pageSize + "&st=" + st + "&end="
					+ end + "&state=" + state;
		}
	}

	// ?????? ???????????? ??????
	@RequestMapping(path = "searchFilterpfileds", method = RequestMethod.GET)
	public String searchFilterpfiled(@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "5") int pageSize, String p_filed, String state, Model model,
			HttpSession session) {
		logger.debug("p_filed{}", p_filed);
		model.addAttribute("alarmList",
				messageService.alarmMessage(((UserVo) session.getAttribute("S_USER")).getUser_id()));
		if (((UserVo) session.getAttribute("S_USER")).getPurpose().equals("C")) {
			model.addAttribute("pList",
					projectService.ingProjectListC(((UserVo) session.getAttribute("S_USER")).getUser_id()));
		} else if (((UserVo) session.getAttribute("S_USER")).getPurpose().equals("P")) {
			model.addAttribute("pList",
					projectService.ingProjectListP(((UserVo) session.getAttribute("S_USER")).getUser_id()));
		}
		if (p_filed == null || p_filed.equals("")) {
			return "redirect:/project/selectProject";
		} else {
			String[] p_fileds = p_filed.split(",");
			SearchVo searchVo = new SearchVo(page, pageSize, p_fileds.length);
			if (p_fileds.length == 1) {
				searchVo.setValue1(p_fileds[0]);
				logger.debug("?????? : {}", searchVo);
			} else if (p_fileds.length == 2) {
				searchVo.setValue1(p_fileds[0]);
				searchVo.setValue2(p_fileds[1]);
				logger.debug("?????? : {}", searchVo);
			} else if (p_fileds.length == 3) {
				searchVo.setValue1(p_fileds[0]);
				searchVo.setValue2(p_fileds[1]);
				searchVo.setValue3(p_fileds[2]);
				logger.debug("?????? : {}", searchVo);
			} else if (p_fileds.length == 4) {
				searchVo.setValue1(p_fileds[0]);
				searchVo.setValue2(p_fileds[1]);
				searchVo.setValue3(p_fileds[2]);
				searchVo.setValue4(p_fileds[3]);
				logger.debug("?????? : {}", searchVo);
			}
			model.addAttribute("state", state);
			model.addAttribute("chk", p_filed);
			logger.debug("p_filed ?????? : {} ", p_filed);
			model.addAttribute("p_filed", p_filed);
			model.addAllAttributes(projectService.searchFilterPfileds(searchVo));
		}
		return "t/project/projectSearch";
	}

//	// ?????? ?????????
//	@RequestMapping("signPage")
//	public String signPage() {
//
//		return "t/project/siginPage";
//	}

	@RequestMapping(path="requestSend", method = RequestMethod.GET)
	public String requestSend(HttpSession session, Model model) {
		logger.debug("???????????????");
		model.addAttribute("projectTList", projectService.requestProjectList(((UserVo) session.getAttribute("S_USER")).getUser_id()));
				
		return "jsonView";
	}
	
	@RequestMapping(path="requestSend", method = RequestMethod.POST)
	public String requestSend(int p_code, String user_id, Model model) {
		
		int cnt = projectService.requestSend(new PAttendVo(p_code, user_id));
		model.addAttribute("cnt", cnt);
		logger.debug("????????????cnt : {} ", cnt);
		return "jsonView";
	}
	
	//???????????? ??? ???????????? ?????? ?????? ???????????? ???????????? ????????? ?????? p_attend ????????? ?????? ??????????????? update ??????. 
	@RequestMapping(path="projectFinishButton" )
	public String projectFinishButton(int p_code , RedirectAttributes ra, Model model) {
		boolean check = true; 
		//??????????????? ????????? ?????????????????? 
		//p_attend ??? project ??????????????? ??????  
		ProjectVo projectVo = projectService.viewProject(p_code); 
		//6??? ???????????? ?????????????????? => ?????? ????????? ???????????????.  = ???????????????????????? ??????????????? ?????? ???????????? ??????. 
		if(!projectVo.getP_state().equals(null) && projectVo.getP_state().equals("06") ) {
			projectService.beforefinishProject(p_code);
			return "redirect:/note/userList?p_code=" + p_code;
			
		}else if(!projectVo.getP_state().equals(null) && projectVo.getP_state().equals("07") )  {
			//07 ?????? ??????????????? ??????. => ???????????? ?????? ????????? ?????????????????? ????????????. 
			//p_attend ???????????? p_state ??? 10 ??? (???????????????) ???????????? ??? (=> 0 ?????? ?????? ????????????)
			int stateIsTenCount = projectService.checkPAttendFinish(p_code); 
				if(stateIsTenCount == 0 ) {
					//?????? ?????????????????? ??????????????? ?????? ????????? 
					logger.debug("?????? ???????????? ????????? ??????"); 
				}else {
					//?????? ??????????????? ????????? ?????? ?????? ?????? ????????? 
					//alert ?????? ??????????????? ???????????? ????????? ??????????????? ?????? 
					logger.debug("?????? ????????? ???????????? ?????? ?????????");
					//???????????? ???????????? ?????? ?????? ????????? ?????? 
					check = false; 
				}
			
			if(check == true) {
				projectService.updateFinishProject(p_code);
				return "redirect:/note/viewMain?p_code=" + p_code; 
			}else {
				ra.addFlashAttribute("msg", "msg");
				return "redirect:/note/userList?p_code=" + p_code;
			}
			
		}
		
		return "redirect:/note/viewMain?p_code=" + p_code; 
		
	}
	
	//???????????? ??? ???????????? ??????????????? ??? ???????????? ?????? ?????? 
	
	@RequestMapping(path="starRating" , method = RequestMethod.POST)
	public String starRating(Model model, EvaluationVo evaluationVo  ) {
		int p_code = evaluationVo.getP_code();
		logger.debug("????????????????????? ??? {}");
		//(service ??? ????????? ??????????????? / service ?????? dao ????????? ??????????????? (???????????? ????????? service??? ????????????))
		//check  ??? ????????? ??????. ???????????? ????????? ?????????. ????????? ????????? ????????? ??????. 
		int checkAlreadyEval = projectService.checkAlreadyEval(evaluationVo); 
		if(checkAlreadyEval == 0 ) {
			//???????????????
			double beforeAverage = (evaluationVo.getActivity() + evaluationVo.getComm() + evaluationVo.getOntime() + evaluationVo.getPro() + evaluationVo.getSat()) / 5.0 ; 
			evaluationVo.setAverage((double)Math.round(beforeAverage * 100) / 100);   
			
			projectService.partnersEvalFinish(evaluationVo); 
			
		}
		return "redirect:/note/userList?p_code=" + p_code ;
	}
}
