-- =====================================================
-- AI-Powered Customer Loyalty Platform
-- Database Initialization Script
-- PostgreSQL 16
-- =====================================================

-- Create database (if not exists - run manually if needed)
-- CREATE DATABASE loyaltydb;

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- =====================================================
-- INDEXES (created after table creation by Hibernate)
-- =====================================================
-- Additional performance indexes beyond JPA-generated ones

-- Customers
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_customers_email ON customers(email);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_customers_city ON customers(city);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_customers_loyalty_tier ON customers(loyalty_tier);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_customers_last_purchase ON customers(last_purchase_date);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_customers_lifetime_value ON customers(lifetime_value);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_customers_name_trgm ON customers USING gin(name gin_trgm_ops);

-- Campaigns
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_campaigns_status ON campaigns(status);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_campaigns_type ON campaigns(campaign_type);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_campaigns_scheduled_at ON campaigns(scheduled_at);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_campaigns_created_at ON campaigns(created_at);

-- Loyalty Transactions
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_loyalty_tx_customer ON loyalty_transactions(customer_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_loyalty_tx_created ON loyalty_transactions(created_at);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_loyalty_tx_type ON loyalty_transactions(transaction_type);

-- Coupons
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_coupons_code ON coupons(code);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_coupons_expiry ON coupons(expiry_date);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_coupons_active ON coupons(is_active);

-- Audit Logs
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_audit_logs_user ON audit_logs(user_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_audit_logs_action ON audit_logs(action);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_audit_logs_created ON audit_logs(created_at);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
