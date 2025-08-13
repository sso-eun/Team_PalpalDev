import React, { useEffect, useState } from 'react';
import Layout from '../components/Layout';
import axios from 'axios';
import { formatDateOnly } from '../utils/dateUtils.js';
import PlaceModal from '../components/PlaceModal.jsx';

const PAGE_GROUP_SIZE = 10;

const TablesPage = () => {
    const [pl, setPl] = useState([]);
    const [page, setPage] = useState(1);
    const [limit] = useState(10);
    const [totalPages, setTotalPages] = useState(1);

    // 모달 상태
    const [modalOpen, setModalOpen] = useState(false);
    const [modalMode, setModalMode] = useState('edit'); // 'edit' | 'delete'
    const [selected, setSelected] = useState(null);

    const openModal = (mode, item) => {
        setModalMode(mode);
        setSelected(item);
        setModalOpen(true);
    };
    const closeModal = () => setModalOpen(false);

    useEffect(() => {
        const fetchPlace = async () => {
            try {
                const res = await axios.get(`/places/admin?page=${page}&limit=${limit}`);
                setPl(Array.isArray(res.data.data) ? res.data.data : []);
                setTotalPages(res.data.totalPages);
            } catch (err) {
                console.error('Error fetching places:', err);
            }
        };
        fetchPlace();
    }, [page, limit]);

    // 페이지네이션 계산
    const currentGroup = Math.floor((page - 1) / PAGE_GROUP_SIZE);
    const startPage = currentGroup * PAGE_GROUP_SIZE + 1;
    const endPage = Math.min(startPage + PAGE_GROUP_SIZE - 1, totalPages);

    const placeLabel = { 0: '병원', 1: '쉼터', 2: '경로당' };

    // 삭제 핸들러 (상대경로로 프록시 통과)
    const handleDelete = async (pl_no) => {
        try {
            await axios.delete(`/places/admin/${encodeURIComponent(String(pl_no))}`);
            setPl((prev) => prev.filter((x) => x.pl_no !== pl_no)); // 낙관적 제거
            closeModal();
        } catch (err) {
            console.error(err);
            alert('삭제 중 오류가 발생했습니다.');
        }
    };

    return (
        <Layout>
            <div className="container-fluid">
                {/* Page Heading */}
                <h1 className="h3 mb-2 text-gray-800">Tables</h1>
                <p className="mb-4">
                    DataTables is a third party plugin that is used to generate the demo table below. For more information about DataTables,
                    please visit the{' '}ㄴ
                    <a target="_blank" href="https://datatables.net">
                        official DataTables documentation
                    </a>
                    .
                </p>
                {/* DataTable */}
                <div className="card shadow mb-4">
                    <div className="card-header py-3">
                        <h6 className="m-0 font-weight-bold text-primary">장소 관리</h6>
                    </div>
                    <div className="card-body">
                        <div className="table-responsive">
                            <table className="table table-bordered" width="100%" cellSpacing="0">
                                <thead>
                                <tr>
                                    <th>번호</th>
                                    <th>구분</th>
                                    <th>장소명</th>
                                    <th>도로명주소</th>
                                    <th>노출여부</th>
                                    <th>수정일</th>
                                    <th>관리</th>
                                </tr>
                                </thead>
                                <tfoot>
                                <tr>
                                    <th>번호</th>
                                    <th>구분</th>
                                    <th>장소명</th>
                                    <th>도로명주소</th>
                                    <th>노출여부</th>
                                    <th>수정일</th>
                                    <th>관리</th>
                                </tr>
                                </tfoot>
                                <tbody>
                                {Array.isArray(pl) &&
                                    pl.map((row) => (
                                        <tr key={row.pl_no}>
                                            <td>{row.pl_no}</td>
                                            <td>{placeLabel[row.pl_type]}</td>
                                            <td>{row.pl_name}</td>
                                            <td>{row.pl_addr}</td>
                                            <td>{row.pl_display === 1 ? '노출' : '비노출'}</td>
                                            <td>{formatDateOnly(row.pl_update)}</td>
                                            <td>
                                                <button
                                                    className="btn btn-sm btn-outline-primary mr-2"
                                                    onClick={() => openModal('edit', row)}
                                                >
                                                    수정
                                                </button>
                                                <button
                                                    className="btn btn-sm btn-outline-danger"
                                                    onClick={() => openModal('delete', row)}
                                                >
                                                    삭제
                                                </button>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>

                            {/* 페이지네이션 */}
                            <div className="pagination-container">
                                {startPage > 1 && (
                                    <button className="page-button" onClick={() => setPage(startPage - PAGE_GROUP_SIZE)}>
                                        &laquo;
                                    </button>
                                )}

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

            {/* 모달 */}
            <PlaceModal
                isOpen={modalOpen}
                mode={modalMode}
                selected={selected}
                onClose={closeModal}
                onDelete={handleDelete}
                onUpdate={(updated) => {
                    setPl((prev) => prev.map((x) => (x.pl_no === updated.pl_no ? { ...x, ...updated } : x)));
                    closeModal();
                }}
            />
        </Layout>
    );
};

export default TablesPage;
