DO $$
DECLARE
    doctor_uuid uuid := '550e8400-e29b-41d4-a716-446655440099';
    manager_uuid uuid := '550e8400-e29b-41d4-a716-446655440098';
    pw_hash text := '$argon2id$v=19$m=16384,t=2,p=1$iU76Q6Or0zFDZk9k4X7HLw$W6Sk+SwieYaLhtqiybjKbA2aD+h/rI5gfjyr3KSSA7E';
BEGIN
    -- Insert Users
    INSERT INTO users (id, username, password, email, full_name, enabled) 
    VALUES (doctor_uuid, 'doctor1', pw_hash, 'doctor@vetimate.com', 'Bác sĩ thú y', true)
    ON CONFLICT DO NOTHING;
    
    INSERT INTO users (id, username, password, email, full_name, enabled) 
    VALUES (manager_uuid, 'manager1', pw_hash, 'manager@vetimate.com', 'Quản lý phòng khám', true)
    ON CONFLICT DO NOTHING;

    -- Insert User Roles
    INSERT INTO user_roles (user_id, role_id) VALUES (doctor_uuid, '550e8400-e29b-41d4-a716-446655440003') ON CONFLICT DO NOTHING;
    INSERT INTO user_roles (user_id, role_id) VALUES (manager_uuid, '550e8400-e29b-41d4-a716-446655440002') ON CONFLICT DO NOTHING;

    -- Insert Staff profiles
    INSERT INTO staff (id, user_id, full_name, role_type, is_active, base_salary, commission_rate, license_number) 
    VALUES (gen_random_uuid(), doctor_uuid, 'Bác sĩ thú y', 'DOCTOR', true, 10000000, 10.0, 'VN-12345')
    ON CONFLICT (user_id) DO NOTHING;
    
    INSERT INTO staff (id, user_id, full_name, role_type, is_active, base_salary, commission_rate, license_number) 
    VALUES (gen_random_uuid(), manager_uuid, 'Quản lý phòng khám', 'MANAGER', true, 20000000, 0, null)
    ON CONFLICT (user_id) DO NOTHING;

END $$;
