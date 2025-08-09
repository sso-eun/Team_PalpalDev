import React from 'react';
import Layout from '../components/Layout';
import DecisionModal from '../components/DecisionModal';
import { useEffect, useState } from 'react';
import axios from 'axios';
import { formatDateTime } from '../utils/dateUtils';
import { SERVERURL  } from '../utils/constants.js';


const TablesPage = () => {
    const [users, setUsers] = useState([]);
    const [page, setPage] = useState(1);
    const [limit] = useState(10);
    const [totalPages, setTotalPages] = useState(1);

    const fetchData = async () => {
        try {
            // const res = await axios.get(`/member/allusers?page=${page}&limit=${limit}`);
            const res = await axios.get(`/cert/list?page=${page}&limit=${limit}`);
            setUsers(res.data.results);
            setTotalPages(res.data.totalPages);
        } catch (err) {
            console.error('Error fetching users:', err);
        }
    };

    useEffect(() => {
        fetchData();
    }, [page, limit]);

    const reviewLabel = {
        0: '대기',
        1: '승인',
        2: '반려'
    };

    //modal ----
    const [modalOpen, setModalOpen] = useState(false);
    const [selectedReqNo, setSelectedReqNo] = useState(null);
    const PAGE_GROUP_SIZE = 10;

    const openModal = (reqNo) => {
        setSelectedReqNo(reqNo);
        setModalOpen(true);
    };

    const handleDecisionSubmit = async ({ reqNo, status, reviewer_note }) => {
    const adminNo = 101; // 임시고정
        try {
            const res = await fetch(`/cert/update/${reqNo}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    status,
                    reviewer_admin_no: adminNo,
                    reviewer_note,
                }),
            });

            const result = await res.json();
            if (result.rsCode === 200) {
                alert(status === 1 ? '승인 완료' : '반려 완료');
                setModalOpen(false);
                fetchData();
            } else {
                alert('처리 실패: ' + result.message);
            }
        } catch (err) {
            console.error(err);
            alert('네트워크 오류');
        }
    };

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
                        <h6 className="m-0 font-weight-bold text-primary">신청 목록</h6>
                    </div>
                    <div className="card-body">
                        <div className="table-responsive">
                            <table className="table table-bordered" id="dataTable" width="100%" cellSpacing="0">
                                <thead>
                                <tr>
                                    <th>순번</th>
                                    <th>신청자 이름</th>
                                    <th>대상자(어르신) 이름</th>
                                    <th>증명서</th>
                                    <th>신청일</th>
                                    <th>승인상태</th>
                                    <th>처리</th>
                                    <th>승인일</th>
                                    <th>검토의견</th>
                                    <th>승인자</th>
                                </tr>
                                </thead>
                                <tfoot>
                                <tr>
                                    <th>순번</th>
                                    <th>신청자 이름</th>
                                    <th>대상자(어르신) 이름</th>
                                    <th>증명서</th>
                                    <th>신청일</th>
                                    <th>승인상태</th>
                                    <th>처리</th>
                                    <th>승인일</th>
                                    <th>검토의견</th>
                                    <th>승인자</th>
                                </tr>
                                </tfoot>
                                <tbody>
                                {users.map((user) => (
                                    <tr key={user.req_no}>
                                        <td>{user.req_no}</td>
                                        <td>{user.guardian_id}</td>
                                        <td>{user.senior_id}</td>
                                        <td>
                                            <a
                                                href={`${SERVERURL}/down/cert/${user.req_no}`}
                                                target="_blank"
                                                rel="noopener noreferrer"
                                                className="btn btn-sm btn-primary"
                                            >
                                                보기
                                            </a>
                                        </td>
                                        <td>{formatDateTime(user.submitted_at)}</td>
                                        <td>{reviewLabel[user.status]}</td>
                                        <td>
                                            {user.status === 0 && (
                                                <button className="btn btn-primary btn-sm" onClick={() => openModal(user.req_no)}>
                                                    처리
                                                </button>
                                            )}
                                        </td>
                                        <td>{formatDateTime(user.reviewed_at)}</td>
                                        <td>{user.reviewer_note || '-'}</td>
                                        <td>{user.reviewer_admin_no || '-'}</td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>

                            <DecisionModal
                                isOpen={modalOpen}
                                onClose={() => setModalOpen(false)}
                                onSubmit={handleDecisionSubmit}
                                reqNo={selectedReqNo}
                            />
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
