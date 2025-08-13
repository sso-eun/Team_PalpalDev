import React from 'react';
import Layout from '../components/Layout';
import { useEffect, useState } from 'react';
import axios from 'axios';
import {formatDateTime} from "../utils/dateUtils.js";

const TablesPage = () => {
    const [users, setUsers] = useState([]);
    const [page, setPage] = useState(1);
    const [limit] = useState(10);
    const [totalPages, setTotalPages] = useState(1);
    const PAGE_GROUP_SIZE = 10;

    useEffect(() => {
        const fetchUsers = async () => {
            try {
                // const res = await axios.get(`/member/allusers?page=${page}&limit=${limit}`);
                const res = await axios.get(`/member/allusers?page=${page}&limit=${limit}`);
                setUsers(res.data.results); // 실제 응답 구조에 따라 조정 필요
                setTotalPages(res.data.totalPages);
            } catch (err) {
                console.error('Error fetching users:', err);
            }
        };

        fetchUsers();
    }, [page, limit]);

    //페이지네이션
    const currentGroup = Math.floor((page - 1) / PAGE_GROUP_SIZE);
    const startPage = currentGroup * PAGE_GROUP_SIZE + 1;
    const endPage = Math.min(startPage + PAGE_GROUP_SIZE - 1, totalPages);


    return (
        <Layout>
            <div className="container-fluid">
                {/* Page Heading */}
                <h1 className="h3 mb-2 text-gray-800">Tables</h1>
                <p className="mb-4">
                    DataTables is a third party plugin that is used to generate the demo table below. For more information about DataTables,
                    please visit the{' '}
                    <a target="_blank" href="https://datatables.net">
                        official DataTables documentation
                    </a>
                    .
                </p>

                {/* DataTales */}
                <div className="card shadow mb-4">
                    <div className="card-header py-3">
                        <h6 className="m-0 font-weight-bold text-primary">DataTables Example</h6>
                    </div>
                    <div className="card-body">
                        <div className="table-responsive">
                            <table className="table table-bordered" id="dataTable" width="100%" cellSpacing="0">
                                <thead>
                                <tr>
                                    <th>고유번호</th>
                                    <th>아이디</th>
                                    <th>연락처</th>
                                    <th>유형</th>
                                    <th>가입일</th>
                                </tr>
                                </thead>
                                <tfoot>
                                <tr>
                                    <th>고유번호</th>
                                    <th>아이디</th>
                                    <th>연락처</th>
                                    <th>유형</th>
                                    <th>가입일</th>
                                </tr>
                                </tfoot>
                                <tbody>

                                {users.map((user) => (
                                    <tr key={user.user_num}>
                                        <td>{user.user_num}</td>
                                        <td>{user.user_id}</td>
                                        <td>{user.user_tel}</td>
                                        <td>{user.user_type === 0 ? '어르신' : '보호자'}</td>
                                        <td>{formatDateTime(user.user_signup)}</td>
                                    </tr>
                                ))}

                                </tbody>
                            </table>

                            {/* 페이지네이션 */}
                            <div className="pagination-container">
                                {/* << 이전 그룹 */}
                                {startPage > 1 && (
                                    <button className="page-button" onClick={() => setPage(startPage - PAGE_GROUP_SIZE)}>
                                        &laquo;
                                    </button>
                                )}

                                {/* 숫자 페이지 버튼 */}
                                {Array.from({ length: endPage - startPage + 1 }, (_, i) => {
                                    const pageNum = startPage + i;
                                    return (
                                        <button
                                            key={pageNum}
                                            className={`page-button ${page === pageNum ? 'active' : ''}`}
                                            onClick={() => setPage(pageNum)}
                                        >
                                            {pageNum}
                                        </button>
                                    );
                                })}

                                {/* >> 다음 그룹 */}
                                {endPage < totalPages && (
                                    <button className="page-button" onClick={() => setPage(endPage + 1)}>
                                        &raquo;
                                    </button>
                                )}
                            </div>

                        </div>
                    </div>
                </div>
            </div>
        </Layout>
    );
};

export default TablesPage;
