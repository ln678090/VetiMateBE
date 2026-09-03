DO $$
DECLARE
    v_doctor_id uuid;
    v_customer_id uuid := gen_random_uuid();
    v_pet1_id uuid := gen_random_uuid();
    v_pet2_id uuid := gen_random_uuid();
    v_pet3_id uuid := gen_random_uuid();
    
    v_svc1_id uuid := gen_random_uuid();
    v_svc2_id uuid := gen_random_uuid();
    v_svc3_id uuid := gen_random_uuid();
    
    v_apt1_id uuid := gen_random_uuid();
    v_apt2_id uuid := gen_random_uuid();
    v_apt3_id uuid := gen_random_uuid();
    
BEGIN
    SELECT id INTO v_doctor_id FROM staff WHERE role_type = 'DOCTOR' LIMIT 1;
    IF v_doctor_id IS NULL THEN
        RAISE NOTICE 'No doctor found!';
        RETURN;
    END IF;

    -- 1. Insert Customer
    INSERT INTO clinic_customers (id, full_name, phone, address, created_at, updated_at) 
    VALUES (v_customer_id, 'Khách hàng Demo', '0912345678', 'Hà Nội', NOW(), NOW());

    -- 2. Insert Pets
    INSERT INTO clinic_pets (id, customer_id, name, species, breed, gender, weight_kg, created_at)
    VALUES 
        (v_pet1_id, v_customer_id, 'Milo', 'DOG', 'Corgi', 'MALE', 5.5, NOW()),
        (v_pet2_id, v_customer_id, 'Kiki', 'CAT', 'Anh lông ngắn', 'FEMALE', 3.2, NOW()),
        (v_pet3_id, v_customer_id, 'Bông', 'DOG', 'Poodle', 'MALE', 4.0, NOW());

    -- 3. Insert Services
    INSERT INTO clinic_services (id, name, description, price, duration_min, is_active)
    VALUES
        (v_svc1_id, 'Khám tổng quát thú cưng', 'Kiểm tra sức khỏe toàn diện', 150000, 30, true),
        (v_svc2_id, 'Tiêm phòng dại 7 bệnh', 'Tiêm vắc xin hàng năm', 250000, 15, true),
        (v_svc3_id, 'Siêu âm thai chó mèo', 'Siêu âm chẩn đoán thai kỳ', 300000, 45, true);

    -- 4. Insert Appointments (Lịch khám)
    INSERT INTO clinic_appointments (id, customer_id, pet_id, service_id, start_at, end_at, duration_min, price_snapshot, status, note, created_at)
    VALUES
        (v_apt1_id, v_customer_id, v_pet1_id, v_svc1_id, NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day' + INTERVAL '30 minutes', 30, 150000, 'COMPLETED', 'Khách đã đến đúng giờ', NOW()),
        (v_apt2_id, v_customer_id, v_pet2_id, v_svc2_id, NOW() + INTERVAL '1 hour', NOW() + INTERVAL '1 hour' + INTERVAL '15 minutes', 15, 250000, 'SCHEDULED', 'Cần tiêm phòng gấp', NOW()),
        (v_apt3_id, v_customer_id, v_pet3_id, v_svc3_id, NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days' + INTERVAL '45 minutes', 45, 300000, 'COMPLETED', 'Chó mẹ mang thai', NOW());

    -- 5. Insert Medical Records (Bệnh án)
    INSERT INTO medical_records (id, appointment_id, pet_id, doctor_id, symptoms, diagnosis, treatment_plan, weight_kg, doctor_note, status, created_at)
    VALUES
        (gen_random_uuid(), v_apt1_id, v_pet1_id, v_doctor_id, 'Mệt mỏi, bỏ ăn 2 ngày', 'Sốt virus nhẹ', 'Truyền dịch, cho uống thuốc hạ sốt', 5.3, 'Theo dõi thêm 3 ngày', 'COMPLETED', NOW()),
        (gen_random_uuid(), v_apt2_id, v_pet2_id, v_doctor_id, 'Đến lịch tiêm phòng', 'Khỏe mạnh, đủ điều kiện tiêm', 'Tiêm 1 mũi 7 bệnh vắc xin Mỹ', 3.2, 'Hẹn lịch nhắc lại sau 1 năm', 'IN_PROGRESS', NOW()),
        (gen_random_uuid(), v_apt3_id, v_pet3_id, v_doctor_id, 'Bụng to, nghi có thai', 'Mang thai 5 tuần, 3 thai nhi khỏe mạnh', 'Bổ sung canxi và vitamin', 4.5, 'Cần siêu âm lại trước khi sinh 1 tuần', 'COMPLETED', NOW());

END $$;
