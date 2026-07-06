package com.allyphone.service;

import com.allyphone.sql.Database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/** CRUD for the {@code phone_accounts} table. */
public class ServicePlanService {

    private final Database database;

    public ServicePlanService(Database database) {
        this.database = database;
    }

    public synchronized void ensureAccount(UUID uuid) throws SQLException {
        try (PreparedStatement ps = database.getConnection().prepareStatement(
                "INSERT OR IGNORE INTO phone_accounts (uuid, plan, last_billed, service_active) VALUES (?,?,?,1)")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, ServicePlan.BASIC.name());
            ps.setLong(3, System.currentTimeMillis());
            ps.executeUpdate();
        }
    }

    public synchronized ServicePlan getPlan(UUID uuid) throws SQLException {
        try (PreparedStatement ps = database.getConnection().prepareStatement(
                "SELECT plan FROM phone_accounts WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? ServicePlan.fromString(rs.getString("plan")) : ServicePlan.BASIC;
            }
        }
    }

    public synchronized void setPlan(UUID uuid, ServicePlan plan) throws SQLException {
        try (PreparedStatement ps = database.getConnection().prepareStatement(
                "UPDATE phone_accounts SET plan = ? WHERE uuid = ?")) {
            ps.setString(1, plan.name());
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        }
    }

    public synchronized boolean isServiceActive(UUID uuid) throws SQLException {
        try (PreparedStatement ps = database.getConnection().prepareStatement(
                "SELECT service_active FROM phone_accounts WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return !rs.next() || rs.getInt("service_active") == 1;
            }
        }
    }

    public synchronized void setServiceActive(UUID uuid, boolean active) throws SQLException {
        try (PreparedStatement ps = database.getConnection().prepareStatement(
                "UPDATE phone_accounts SET service_active = ? WHERE uuid = ?")) {
            ps.setInt(1, active ? 1 : 0);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        }
    }

    public synchronized long getLastBilled(UUID uuid) throws SQLException {
        try (PreparedStatement ps = database.getConnection().prepareStatement(
                "SELECT last_billed FROM phone_accounts WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong("last_billed") : 0L;
            }
        }
    }

    public synchronized void setLastBilled(UUID uuid, long timestamp) throws SQLException {
        try (PreparedStatement ps = database.getConnection().prepareStatement(
                "UPDATE phone_accounts SET last_billed = ? WHERE uuid = ?")) {
            ps.setLong(1, timestamp);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
        }
    }
}
