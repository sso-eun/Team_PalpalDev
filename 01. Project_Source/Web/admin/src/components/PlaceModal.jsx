import React from 'react';
import EditForm from './EditForm';

const PlaceModal = ({ isOpen, mode, selected, onClose, onDelete, onUpdate }) => {
    if (!isOpen || !selected) return null;

    const title = mode === 'edit' ? '장소 수정' : '장소 삭제';

    return (
        <>
            <div className="modal fade show" style={{ display: 'block' }} tabIndex="-1" role="dialog" aria-modal="true">
                <div className="modal-dialog modal-dialog-centered" role="document">
                    <div className="modal-content">
                        <div className="modal-header">
                            <h5 className="modal-title">{title}</h5>
                            <button type="button" className="close" aria-label="Close" onClick={onClose}>
                                <span aria-hidden="true">&times;</span>
                            </button>
                        </div>

                        <div className="modal-body">
                            {mode === 'edit' && (
                                <EditForm
                                    initial={selected}
                                    onCancel={onClose}
                                    onSaved={(updated) => {
                                        onUpdate?.(updated);
                                    }}
                                />
                            )}

                            {mode === 'delete' && (
                                <>
                                    <p>
                                        <b>{selected.pl_name}</b> 항목을 삭제하시겠습니까?
                                    </p>
                                    <div className="d-flex justify-content-end">
                                        <button className="btn btn-secondary mr-2" onClick={onClose}>취소</button>
                                        <button className="btn btn-danger" onClick={() => onDelete?.(selected.pl_no)}>삭제</button>
                                    </div>
                                </>
                            )}
                        </div>
                    </div>
                </div>
            </div>

            {/* 배경 클릭으로 닫기 */}
            <div className="modal-backdrop fade show" onClick={onClose}></div>
        </>
    );
};

export default PlaceModal;
