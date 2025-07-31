import React from 'react';
import Layout from '../components/Layout';
import { useEffect, useState } from 'react';
import axios from 'axios';

const TablesPage = () => {
    const [users, setUsers] = useState([]);
    const [page, setPage] = useState(1);
    const [limit] = useState(10);
    const [totalPages, setTotalPages] = useState(1);

    useEffect(() => {
        const fetchUsers = async () => {
            try {
                // const res = await axios.get(`/member/allusers?page=${page}&limit=${limit}`);
                const res = await axios.get(`/member/allusers?page=${page}&limit=${limit}`);
                setUsers(res.data.results); // 실제 응답 구조에 따라 조정 필요
                console.log(res.data);
                setTotalPages(res.data.totalPages);
            } catch (err) {
                console.error('Error fetching users:', err);
            }
        };

        fetchUsers();
    }, [page, limit]);

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

                {/* DataTales Example */}
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
                                        <td>{new Date(user.user_signup).toLocaleDateString('ko-KR')}</td>
                                    </tr>
                                ))}

                                </tbody>
                            </table>

                            {/* 페이지네이션 버튼 (예시) */}
                            <div>
                                {Array.from({ length: totalPages }, (_, i) => (
                                    <button key={i+1} onClick={() => setPage(i + 1)}>{i + 1}</button>
                                ))}
                            </div>

                        </div>
                    </div>
                </div>
            </div>
        </Layout>
    );
};

export default TablesPage;
