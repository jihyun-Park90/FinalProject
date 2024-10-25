import React, { useEffect, useState } from 'react';
import './css/course_mu.css';
import axios from 'axios';

// 기본 axios 헤더 설정
axios.defaults.headers.common['Access-Control-Allow-Origin'] = 'http://localhost:3000';

const Sidebar = ({ courseId }) => {
	const [baseUrl, setBaseUrl] = useState('');
	const [menuVisible, setMenuVisible] = useState(false);
	const [menuName, setMenuName] = useState('');
	const [userRole, setUserRole] = useState(null);

	useEffect(() => {
		const { protocol, hostname } = window.location;
		setBaseUrl(`${protocol}//${hostname}:8080`);

		const role = sessionStorage.getItem('userRole');
		setUserRole(role ? Number(role) : null);

		if (courseId) {
			fetchCourseName(courseId);
		}
	}, [courseId]);

	const fetchCourseName = async (id) => {
		try {
			const response = await axios.get(`${baseUrl}/api/courseName/${id}`);
			setMenuName(response.data);
		} catch (error) {
			console.error('Error fetching course name:', error);
		}
	};

	const toggleMenu = () => {
		setMenuVisible(prev => !prev);
	};

	const handleBackButtonClick = () => {
		window.history.back();
	};

	return (
		<aside>
			<div className="sc_course-sidebar-left">
				<ul className="sc_sidebar-menu">
					<li>
						<a href={`${baseUrl}/mypage`}>
							<img className="sc_sidebar-icon" src={require('./img/mypage.png')} alt="마이 페이지" />
						</a>
					</li>
					<li>
						<a href={`${baseUrl}/`}>
							<img className="sc_sidebar-icon" src={require('./img/course.png')} alt="코스" />
						</a>
					</li>
					<li>
						<a href={`${baseUrl}/#`}>
							<img className="sc_sidebar-icon" src={require('./img/attend.png')} alt="출석 체크" />
						</a>
					</li>
					<li>
						<a href={`${baseUrl}/register`}>
							<img className="sc_sidebar-icon" src={require('./img/register.png')} alt="수강 신청" />
						</a>
					</li>
					<li>
						<a href={`${baseUrl}/notice`}>
							<img className="sc_sidebar-icon" src={require('./img/notice.png')} alt="공지사항" />
						</a>
					</li>
					<li>
						<a href={`${baseUrl}/logout`}>
							<img className="sc_sidebar-icon" src={require('./img/logout.png')} alt="로그아웃" />
						</a>
					</li>
				</ul>
			</div>

			<div className="sc_course-sidebar-right">
				<div className="sc_sidebar-logo">
					<a href={`${baseUrl}/`}>
						<h1>CHECK</h1>
					</a>
					<h3>{menuName}</h3>
					<hr />
				</div>

				<div id="sc_mobile-menu">
					<h1 id="sc_backBtn" onClick={handleBackButtonClick}>◀</h1>
					<h1 className="sc_menuName">{menuName}</h1>
					<div id="sc_mobile-menu-icon" onClick={toggleMenu}>
						<svg xmlns="http://www.w3.org/2000/svg" width="30px" height="30px" viewBox="0 0 24 24" fill="none">
							<path d="M4 6H20M4 12H20M4 18H20" stroke="#000000" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
						</svg>
					</div>
				</div>

				<ul className="sc_sidebar-menu">
					<a href={`${baseUrl}/goCourseHome?courseId=${courseId}`} className="sc_sidebar-menu-unselected" style={{ width: '33.333%' }}>
						<li>홈</li>
					</a>
					<a href={`/CourseBoard?courseId=${courseId}`} className="sc_sidebar-menu-selected" style={{ width: '33.333%' }}>
						<li>강의 게시판</li>
					</a>
					{(userRole === 1) && (
						<>
							<a href={`${baseUrl}/goAttendanceCalendar?courseId=${courseId}`} className="sc_sidebar-menu-unselected" style={{ width: '33.333%' }}>
								<li>출결 확인</li>
							</a>
						</>
					)}
					{(userRole === 0 || userRole === 2) && (
						<>
							<a href={`${baseUrl}/goCurrentAttendance?courseId=${courseId}`} className="sc_sidebar-menu-unselected" style={{ width: '33.333%' }}>
								<li>출결 확인</li>
							</a>
							<a href={`${baseUrl}/acceptanceManagement`} className="sc_sidebar-menu-unselected" style={{ width: '33.333%' }}>
								<li>수강 신청 관리</li>
							</a>
							<a href={`${baseUrl}/courseAttend`} className="sc_sidebar-menu-unselected" style={{ width: '33.333%' }}>
								<li>강의 일정 관리</li>
							</a>
						</>
					)}
				</ul>

				<ul className="sc_menulist" id="sc_menuList" style={{ visibility: menuVisible ? 'visible' : 'hidden' }}>
					<li className="sc_menu sc_cffffff"><a href={`${baseUrl}/`}>코스</a></li>
					<li className="sc_menu sc_cffffff"><a href={`${baseUrl}/checkin`}>출석 체크</a></li>
					<li className="sc_menu sc_cffffff"><a href={`${baseUrl}/register`}>수강 신청</a></li>
					<li className="sc_menu sc_cffffff"><a href={`${baseUrl}/alert`}>알림</a></li>
					<li className="sc_menu sc_cffffff"><a href={`${baseUrl}/notice`}>공지사항</a></li>
					<li className="sc_menu sc_cffffff"><a href={`${baseUrl}/mypage`}>마이 페이지</a></li>
					<li className="sc_menu sc_cffffff"><a href={`${baseUrl}/logout`}>로그아웃</a></li>
				</ul>
			</div>
		</aside>
	);
};

export default Sidebar;
