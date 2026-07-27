-- Add idempotency guard for wallet ledger entries sourced from payment/reference events.
ALTER TABLE wallet_ledgers
    ADD CONSTRAINT uk_wallet_ledger_idempotency UNIQUE (wallet_id, reference_id, category);
