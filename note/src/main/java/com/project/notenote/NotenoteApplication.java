package com.project.notenote;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com")
public class NotenoteApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotenoteApplication.class, args);
	}
	// - ถ้ามีการเข้าถึง endpoint ที่ไม่ได้อนุญาต จะโดน spring security block และไม่มีการส่ง error มาที่ client = ส่งมาแล้วแต่ยังไม่แสดงใน html
	
	// - users and note relationship (เสร็จแล้วแต่ไม่น่าจะมีอะไรต่ออีก เพราะ Note จัดการแล้ว)
	
	// - มันมีปัญหาว่า เราใช้ 
	// getMapping
	// แล้ว validate token จากนั้นถ้าตรงให้ไปหน้า welcome ถ้าไม่ตรงให้ไปหน้า /auth/login
	// แต่ความเป็นจริงจะมีการพิมพ์เข้า /auth/login โดยตรง ทำให้ส่วนนี้ไม่มีหาร validate token ส่งผลให้ต้องล็อคอินใหม่
	// ถ้าจะทำ validate token ใน /auth/login ก็จะเกิด duplicate หรือการทำโค้ดซ้ำ
	// จะแก้ยังไง
	
	// when user at in home page then client request all notes and user information with token for authentication
	// when user click some note then client request data of that note with token for authentication
	// 
	
	// (success)
	// - ทำ access, refresh token
	// - เปลี่ยน enpoint จาก auth เป็น api
	// - handle กรณีที่ register เข้ามาแล้วชื่อมันซ้ำกัน จะต้อง return error กลับไปด้วย
	// - ลบ token เมื่อมีการ logout
	// - forget password
	


	// ถ้าใช้วิธีนี้ รอบที่สองสามสี่ห้า จะมีลำดับการสำรวจโหนดใดบ้าง ต้องไล้ algorithm ให้ถูก ข้อนี้ยากสุด
	// chalenge, พื้นฐาน
	// 
}
