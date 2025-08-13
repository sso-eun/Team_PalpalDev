import React, { useState } from 'react';

const DecisionModal = ({ isOpen, onClose, onSubmit, reqNo }) => {
    const [reviewNote, setReviewNote] = useState('');

    const handleSubmit = (status) => {
        if (status === 2 && !reviewNote.trim()) {
            alert('반려 사유를 입력해주세요.');
            return;
        }
        onSubmit({ reqNo, status, reviewer_note: reviewNote });
        setReviewNote('');
    };

    if (!isOpen) return null;

    return (
        <div className="modal show d-block modal-overlay">
            <div className="modal-dialog ">
                <div className="modal-content">
                    <div className="modal-header">
                        <h5 className="modal-title">신청 처리</h5>
                        <button className="btn-close" onClick={onClose}></button>
                    </div>
                    <div className="modal-body">
                        <label className="form-label">검토 의견 (반려 시 필수)</label>
                        <textarea
                            className="form-control"
                            value={reviewNote}
                            onChange={(e) => setReviewNote(e.target.value)}
                            rows={4}
                            placeholder="검토 의견을 입력하세요"
                        />
                    </div>
                    <div className="modal-footer">
                        <button className="btn btn-secondary" onClick={onClose}>닫기</button>
                        <button className="btn btn-danger" onClick={() => handleSubmit(2)}>반려</button>
                        <button className="btn btn-success" onClick={() => handleSubmit(1)}>승인</button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default DecisionModal;
