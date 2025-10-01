const form = document.getElementById("registerForm");
const password = document.getElementById("password");
const confirmPassword = document.getElementById("confirmPassword");
const passwordMatch = document.getElementById("passwordMatch");

form.addEventListener("submit", function(e) {
  if (password.value !== confirmPassword.value) {
    e.preventDefault(); // หยุด form ไม่ให้ submit
    passwordMatch.style.display = "block"; // แสดงข้อความ error
    confirmPassword.focus(); // focus ที่ช่องยืนยันรหัสผ่าน
  } else {
    passwordMatch.style.display = "none"; // ซ่อน error
    // form จะ submit ปกติ
  }
});

