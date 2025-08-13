import React from 'react';
import Layout from '../components/Layout';
import { useEffect, useState } from 'react';
import axios from 'axios';
import {formatDateTime} from "../utils/dateUtils.js";

const TablesPage = () => {
    const [pl, setPl] = useState([]);
    const [page, setPage] = useState(1);
    const [limit] = useState(10);
    const [totalPages, setTotalPages] = useState(1);
    const PAGE_GROUP_SIZE = 10;

    useEffect(() => {
        const fetchPlace= async () => {
            try {
                // const res = await axios.get(`/member/allusers?page=${page}&limit=${limit}`);
                const res = await axios.get(`/places/admin?page=${page}&limit=${limit}`);
                console.log('API 전체 응답:', res.data);
                setPl(Array.isArray(res.data.data) ? res.data.data : []);
                setTotalPages(res.data.totalPages);
            } catch (err) {
                console.error('Error fetching users:', err);
            }
        };

        fetchPlace();
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
                                    <th>장소구분</th>
                                    <th>장소명</th>
                                    <th>우편번호</th>
                                    <th>도로명주소</th>
                                    <th>상세주소</th>
                                    <th>연락처</th>
                                    <th>노출여부</th>
                                    <th>등록일</th>
                                    <th>수정일</th>
                                </tr>
                                </thead>
                                <tfoot>
                                <tr>
                                    <th>고유번호</th>
                                    <th>장소구분</th>
                                    <th>장소명</th>
                                    <th>우편번호</th>
                                    <th>도로명주소</th>
                                    <th>상세주소</th>
                                    <th>연락처</th>
                                    <th>노출여부</th>
                                    <th>등록일</th>
                                    <th>수정일</th>
                                </tr>
                                </tfoot>
                                <tbody>

                                {Array.isArray(pl) && pl.map((pl) => (
                                    <tr key={pl.pl_no}>
                                        <td>{pl.pl_no}</td>
                                        <td>{pl.pl_type}</td>
                                        <td>{pl.pl_name}</td>
                                        <td>{pl.pl_postNumber}</td>
                                        <td>{pl.pl_addr}</td>
                                        <td>{pl.pl_detailAddr}</td>
                                        <td>{pl.pl_tel}</td>
                                        <td>{pl.pl_display === 1 ? '노출' : '비노출'}</td>
                                        <td>{formatDateTime(pl.pl_write)}</td>
                                        <td>{formatDateTime(pl.pl_update)}</td>
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
