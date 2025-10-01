document.addEventListener('DOMContentLoaded', () => {
    const createNoteBtn = document.getElementById('createNoteBtn');
    const updateNoteBtn = document.getElementById('updateNoteBtn');
    const deleteNoteBtn = document.getElementById('deleteNoteBtn');
    const apiResponseDiv = document.getElementById('apiResponse');

    function displayResponse(data, status = 'Success') {
        const timestamp = new Date().toLocaleTimeString();
        apiResponseDiv.innerHTML = `[${timestamp}] ${status}:\n` + JSON.stringify(data, null, 2);
        console.log(`[${timestamp}] ${status}:`, data);
    }

    // ฟังก์ชันสำหรับแสดงข้อผิดพลาด
    function displayError(error, status = 'Error') {
        const timestamp = new Date().toLocaleTimeString();
        apiResponseDiv.innerHTML = `[${timestamp}] ${status}:\n` + (error.message || JSON.stringify(error, null, 2));
        console.error(`[${timestamp}] ${status}:`, error);
    }

    createNoteBtn.addEventListener('click', async () => {
        const noteTitle = prompt('ใส่ชื่อโน้ตใหม่:', 'โน้ตใหม่จากเว็บ');
        if (!noteTitle) return;

        const noteData = { noteTitle: noteTitle};

        try {
            const response = await fetch('http://localhost:8081/notes/create', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(noteData)
            });

            if (response.ok) {
                const data = await response.json();
                lastCreatedNoteId = data.id; // เก็บ ID เพื่อใช้อ้างอิง
                displayResponse(data, 'Note Created (POST)');
                alert(`สร้างโน้ตสำเร็จ! ID: ${lastCreatedNoteId}`);
            } else {
                const errorData = await response.json();
                displayError(errorData, `Create Failed (Status: ${response.status})`);
            }
        } catch (error) {
            displayError(error, 'Network Error during Create');
        }
    });

    updateNoteBtn.addEventListener('click', async () => {
        const noteId = prompt('ใส่ ID ของโน้ตที่ต้องการอัปเดต:', lastCreatedNoteId || '');
        if (!noteId) return;

        const noteData = { noteTitle: prompt('ใส่ชื่อโน้ตใหม่:', 'โน้ตใหม่จากเว็บ') };
        if (!noteData.noteTitle) return;

        try {
            const response = await fetch(`http://localhost:8081/notes/update/${noteId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(noteData)
            });

            if (response.ok) {
                const data = await response.json();
                displayResponse(data, 'Note Updated (PUT)');
                alert(`อัปเดตโน้ตสำเร็จ! ID: ${noteId}`);
            } else {
                const errorData = await response.json();
                displayError(errorData, `Update Failed (Status: ${response.status})`);
            }
        } catch (error) {
            displayError(error, 'Network Error during Update');
        }
    });
    deleteNoteBtn.addEventListener('click', async () => {
        const noteId = prompt('ใส่ ID ของโน้ตที่ต้องการลบ:', lastCreatedNoteId || '');
        if (!noteId) return;

        try {
            const response = await fetch(`http://localhost:8081/notes/delete/${noteId}`, {
                method: 'DELETE',
                headers: { 'Content-Type': 'application/json' }
            });

            if (response.ok) {
                const data = await response.json();
                displayResponse(data, 'Note Deleted (DELETE)');
                alert(`ลบโน้ตสำเร็จ! ID: ${noteId}`);
            } else {
                const errorData = await response.json();
                displayError(errorData, `Delete Failed (Status: ${response.status})`);
            }
        } catch (error) {
            displayError(error, 'Network Error during Delete');
        }
    });
});