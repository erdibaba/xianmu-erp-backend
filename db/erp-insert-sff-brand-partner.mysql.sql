-- Insert Silver Fern Farms as a BRAND partner; keep the same-name P0026 SECONDARY partner unchanged.

INSERT INTO `erp_partner` (
  `partner_code`,
  `partner_name`,
  `partner_type`,
  `business_role`,
  `cold_storage_free_days`,
  `tax_no`,
  `bank_name`,
  `bank_account`,
  `address`,
  `contact_name`,
  `contact_phone`,
  `contact_email`,
  `remark`,
  `status`,
  `create_user_id`,
  `create_time`,
  `update_time`
)
SELECT
  'P0001',
  _utf8mb4 0xE993B6E4B98BE895A8E9A39FE59381EFBC88E4B88AE6B5B7EFBC89E69C89E99990E585ACE58FB8,
  1,
  'BRAND',
  7,
  '91310115MA1HAE2L1K',
  _utf8mb4 0xE4B8ADE4BFA1E993B6E8A18CE882A1E4BBBDE69C89E99990E585ACE58FB8E4B88AE6B5B7E899B9E6A1A5E59586E58AA1E58CBAE694AFE8A18C,
  '8110201012201206249',
  NULL,
  NULL,
  NULL,
  NULL,
  _utf8mb4 0xE59381E7898CE696B9,
  1,
  1,
  NOW(),
  NOW()
WHERE NOT EXISTS (
  SELECT 1 FROM `erp_partner` WHERE `partner_code` = 'P0001'
);
