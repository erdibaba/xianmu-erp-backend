CREATE TABLE IF NOT EXISTS `erp_presale_attachment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `presale_order_id` bigint NOT NULL COMMENT '预销售单ID',
  `attachment_type` varchar(32) NOT NULL COMMENT '附件类型(CUSTOMS报关单/QUARANTINE检疫证明)',
  `file_path` varchar(500) NOT NULL COMMENT '归档文件路径',
  `file_name` varchar(255) NOT NULL COMMENT '归档文件名称',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_user_id` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_presale_order_type` (`presale_order_id`, `attachment_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预销售单附件归档表';
