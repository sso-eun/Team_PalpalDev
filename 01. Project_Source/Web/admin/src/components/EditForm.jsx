import React, { useState } from 'react';
import axios from 'axios';

const EditForm = ({ initial, onCancel, onSaved }) => {
    const [form, setForm] = useState({
        pl_name:       initial?.pl_name ?? '',
        pl_postNumber: initial?.pl_postNumber ?? '',
        pl_addr:       initial?.pl_addr ?? '',
        pl_detailAddr: initial?.pl_detailAddr ?? '',
        pl_tel:        initial?.pl_tel ?? '',
        pl_lat:        initial?.pl_lat ?? '',
        pl_lon:        initial?.pl_lon ?? '',
        pl_type:       initial?.pl_type ?? 0, // 0:hospital,1:shelter,2:care
        pl_display:    initial?.pl_display ?? 1, // 1:노출,0:비노출
    });
    const [submitting, setSubmitting] = useState(false);

    // 숫자 변환이 필요한 키
    const numericKeys = new Set(['pl_lat', 'pl_lon', 'pl_type', 'pl_display']);

    const onChange = (e) => {
        const { name, value } = e.target;
        let v = value;
        if (numericKeys.has(name)) {
            // 빈 문자열은 그대로, 값이 있으면 number로 변환
            v = value === '' ? '' : Number(value);
        }
        setForm(prev => ({ ...prev, [name]: v }));
    };

    const onSubmit = async (e) => {
        e.preventDefault();
        try {
            const id = initial?.pl_no;
            if (id === undefined || id === null || id === '') {
                throw new Error('pl_no가 없습니다.');
            }
            setSubmitting(true);

            // 변경된 필드만 추려서 PATCH
            const payload = Object.fromEntries(
                Object.entries(form).filter(([k, v]) => v !== initial?.[k])
            );

            // 아무 것도 안 바뀌었으면 서버 호출 안함
            if (Object.keys(payload).length === 0) {
                onSaved?.(initial);
                setSubmitting(false);
                return;
            }

            const url = `/places/admin/${encodeURIComponent(String(id))}`;
            const res = await axios.patch(url, payload, {
                headers: { 'Content-Type': 'application/json' },
            });

            onSaved?.({ ...initial, ...payload });
            alert(res?.data?.message || '장소 일부 수정 완료');
        } catch (err) {
            console.error(err);
            alert(err?.message || '수정 중 오류가 발생했습니다.');
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <form onSubmit={onSubmit}>
            <div className="form-group">
                <label>장소명</label>
                <input className="form-control" name="pl_name" value={form.pl_name} onChange={onChange}/>
            </div>

            <div className="form-group">
                <label>우편주소</label>
                <input className="form-control" name="pl_postNumber" value={form.pl_postNumber} onChange={onChange}/>
            </div>

            <div className="form-group">
                <label>장소 주소</label>
                <input className="form-control" name="pl_addr" value={form.pl_addr} onChange={onChange}/>
            </div>

            <div className="form-group">
                <label>상세 주소</label>
                <input className="form-control" name="pl_detailAddr" value={form.pl_detailAddr} onChange={onChange}/>
            </div>

            <div className="form-group">
                <label>연락처</label>
                <input className="form-control" name="pl_tel" value={form.pl_tel} onChange={onChange}/>
            </div>

            <div className="form-row">
                <div className="form-group col">
                    <label>위도</label>
                    <input type="number" step="any" className="form-control" name="pl_lat" value={form.pl_lat} onChange={onChange}/>
                </div>
                <div className="form-group col">
                    <label>경도</label>
                    <input type="number" step="any" className="form-control" name="pl_lon" value={form.pl_lon} onChange={onChange}/>
                </div>
            </div>

            <div className="form-row">
                <div className="form-group col">
                    <label>장소 분류</label>
                    <select className="form-control" name="pl_type" value={form.pl_type} onChange={onChange}>
                        <option value={0}>hospital</option>
                        <option value={1}>shelter</option>
                        <option value={2}>care</option>
                    </select>
                </div>

                <div className="form-group col">
                    <label>표시 여부</label>
                    <select className="form-control" name="pl_display" value={form.pl_display} onChange={onChange}>
                        <option value={1}>노출</option>
                        <option value={0}>비노출</option>
                    </select>
                </div>
            </div>

            <div className="d-flex justify-content-end">
                <button type="button" className="btn btn-secondary mr-2" onClick={onCancel} disabled={submitting}>
                    취소
                </button>
                <button type="submit" className="btn btn-primary" disabled={submitting}>
                    {submitting ? '저장 중…' : '저장'}
                </button>
            </div>
        </form>
    );
};

export default EditForm;
