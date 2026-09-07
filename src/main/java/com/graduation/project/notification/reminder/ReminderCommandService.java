// package com.graduation.project.notification.reminder;
//
// import org.springframework.dao.DataIntegrityViolationException;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;
//
// import java.time.LocalDate;
// import java.util.Objects;
// import java.util.UUID;
//
// @Service
// public class ReminderCommandService {
//
// private final ZaloReminderRepository reminderRepository;
//
// public ReminderCommandService(
// ZaloReminderRepository reminderRepository) {
// this.reminderRepository = reminderRepository;
// }
//
// @Transactional
// public ZaloReminder scheduleVaccination(
// UUID customerId,
// UUID petId,
// String phone,
// String petName,
// LocalDate dueDate) {
// return schedule(
// customerId,
// petId,
// phone,
// petName,
// ReminderType.VACCINATION,
// dueDate);
// }
//
// @Transactional
// public ZaloReminder scheduleFollowUp(
// UUID customerId,
// UUID petId,
// String phone,
// String petName,
// LocalDate dueDate) {
// return schedule(
// customerId,
// petId,
// phone,
// petName,
// ReminderType.FOLLOW_UP,
// dueDate);
// }
//
// private ZaloReminder schedule(
// UUID customerId,
// UUID petId,
// String phone,
// String petName,
// ReminderType reminderType,
// LocalDate dueDate) {
// validate(
// customerId,
// petId,
// phone,
// petName,
// reminderType,
// dueDate);
//
// String idempotencyKey = createIdempotencyKey(
// petId,
// reminderType,
// dueDate);
//
// return reminderRepository
// .findByIdempotencyKey(idempotencyKey)
// .orElseGet(() -> createReminder(
// customerId,
// petId,
// phone,
// petName,
// reminderType,
// dueDate,
// idempotencyKey));
// }
//
// private ZaloReminder createReminder(
// UUID customerId,
// UUID petId,
// String phone,
// String petName,
// ReminderType reminderType,
// LocalDate dueDate,
// String idempotencyKey) {
// try {
// ZaloReminder reminder = ZaloReminder.create(
// customerId,
// petId,
// phone.trim(),
// petName.trim(),
// reminderType,
// dueDate);
//
// return reminderRepository.saveAndFlush(reminder);
// } catch (DataIntegrityViolationException exception) {
// /*
// * Transaction đồng thời có thể đã tạo cùng reminder.
// * Unique constraint idempotency_key là lớp bảo vệ cuối.
// */
// return reminderRepository
// .findByIdempotencyKey(idempotencyKey)
// .orElseThrow(() -> exception);
// }
// }
//
// private void validate(
// UUID customerId,
// UUID petId,
// String phone,
// String petName,
// ReminderType reminderType,
// LocalDate dueDate) {
// Objects.requireNonNull(customerId, "customerId is required");
// Objects.requireNonNull(petId, "petId is required");
// Objects.requireNonNull(
// reminderType,
// "reminderType is required");
// Objects.requireNonNull(dueDate, "dueDate is required");
//
// if (phone == null || phone.isBlank()) {
// throw new IllegalArgumentException("phone is required");
// }
//
// if (phone.length() > 20) {
// throw new IllegalArgumentException(
// "phone must not exceed 20 characters");
// }
//
// if (petName == null || petName.isBlank()) {
// throw new IllegalArgumentException("petName is required");
// }
//
// if (petName.length() > 100) {
// throw new IllegalArgumentException(
// "petName must not exceed 100 characters");
// }
// }
//
// private String createIdempotencyKey(
// UUID petId,
// ReminderType reminderType,
// LocalDate dueDate) {
// return reminderType.name()
// + ":"
// + petId
// + ":"
// + dueDate;
// }
// }
