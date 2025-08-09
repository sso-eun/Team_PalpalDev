import React from 'react';
import { NavLink } from 'react-router-dom';
import { Link } from 'react-router-dom';
/* using drop down */
import { useState } from 'react';

function Sidebar() {
    const [openMenus, setOpenMenus] = useState({
        components: false,
        members: false,
        settings: false,
        place: false,
        kiosk: false,
    });

    /* 메뉴명 일치 검사해서 같은 메뉴ㄴ면 드롭다운 */
    const toggleMenu = (menuName) => {
        setOpenMenus((prev) => ({
            ...prev,
            [menuName]: !prev[menuName],
        }));
    };

    return (
        <ul className="navbar-nav bg-gradient-primary sidebar sidebar-dark accordion" id="accordionSidebar">

            {/* Sidebar - Brand */}
            <a className="sidebar-brand d-flex align-items-center justify-content-center" href="/">
                <div className="sidebar-brand-icon rotate-n-15">
                    <i className="fas fa-laugh-wink"></i>
                </div>
                <div className="sidebar-brand-text mx-3">
                    PalPalDev Admin
                </div>
            </a>

            {/* Divider */}
            <hr className="sidebar-divider my-0" />

            {/* Nav Item - Dashboard */}
            <li className="nav-item active">
                <NavLink className="nav-link" to="/">
                    <i className="fas fa-fw fa-tachometer-alt"></i>
                    <span>Dashboard</span>
                </NavLink>
            </li>

            {/* Divider */}
            <hr className="sidebar-divider" />

            {/* Heading */}
            <div className="sidebar-heading">Interface</div>

            {/* Nav Item - Components Collapse Menu */}
            <li className="nav-item">
                <a
                    className="nav-link collapsed"
                    href="#"
                    data-toggle="collapse"
                    data-target="#collapseTwo"
                    aria-expanded="false"
                    aria-controls="collapseTwo">

                    <i className="fas fa-fw fa-cog"></i>
                    <span>Components</span>
                </a>
                <div
                    id="collapseTwo"
                    className="collapse"
                    aria-labelledby="headingTwo"
                    data-parent="#accordionSidebar">

                    <div className="bg-white py-2 collapse-inner rounded">
                        <h6 className="collapse-header">Custom Components:</h6>
                        <NavLink className="collapse-item" to="/components/buttons">
                            Buttons
                        </NavLink>
                        <NavLink className="collapse-item" to="/components/cards">
                            Cards
                        </NavLink>
                    </div>
                </div>
            </li>

            {/* TODO */}
            {/* Members 메뉴 */}
            <li className="nav-item">
                <a
                    className={`nav-link d-flex align-items-center ${!openMenus.members ? 'collapsed' : ''}`}
                    href="#!"
                    onClick={() => toggleMenu('members')}
                    aria-expanded={openMenus.members}
                    aria-controls="collapseUser">

                    <i className="fas fa-fw fa-user"></i>
                    <span>회원</span>
                    <span className="ml-auto">
                         {openMenus.members ? <i className="fas fa-angle-down" /> : <i className="fas fa-angle-right" />}
                    </span>
                </a>

                <div
                    id="collapseUser"
                    className={`collapse-menu collapse ${openMenus.members ? 'show' : ''}`}>
                    <div className="bg-white py-2 collapse-inner rounded">
                        <h6 className="collapse-header">회원 관리 :</h6>
                        <NavLink className="collapse-item" to="/memberlist">
                            전체 회원
                        </NavLink>
                        <NavLink className="collapse-item" to="/authlist">
                            보호자 인증
                        </NavLink>
                    </div>
                </div>
            </li>

            {/* place 메뉴 */}
            <li className="nav-item">
                <a
                    className={`nav-link d-flex align-items-center ${!openMenus.place ? 'collapsed' : ''}`}
                    href="#!"
                    onClick={() => toggleMenu('place')}
                    aria-expanded={openMenus.place}
                    aria-controls="collapseUser">

                    <i className="fas fa-fw fa-user"></i>
                    <span>장소 관리</span>
                    <span className="ml-auto">
                         {openMenus.place ? <i className="fas fa-angle-down" /> : <i className="fas fa-angle-right" />}
                    </span>
                </a>
                <div
                    id="collapseUser"
                    className={`collapse-menu collapse ${openMenus.place ? 'show' : ''}`}>
                    <div className="bg-white py-2 collapse-inner rounded">
                        <h6 className="collapse-header">장소</h6>
                        <NavLink className="collapse-item" to="/placelist">
                            등록장소
                        </NavLink>
                    </div>
                </div>
            </li>

            {/* kiosk 메뉴 */}
            <li className="nav-item">
                <a
                    className={`nav-link d-flex align-items-center ${!openMenus.kiosk ? 'collapsed' : ''}`}
                    href="#!"
                    onClick={() => toggleMenu('place')}
                    aria-expanded={openMenus.kiosk}
                    aria-controls="collapseUser">

                    <i className="fas fa-fw fa-user"></i>
                    <span>키오스크 영상 관리</span>
                    <span className="ml-auto">
                         {openMenus.kiosk ? <i className="fas fa-angle-down" /> : <i className="fas fa-angle-right" />}
                    </span>
                </a>
            </li>

        </ul>
    );
}

export default Sidebar;
