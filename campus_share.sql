/*
 Navicat Premium Data Transfer

 Source Server         : root
 Source Server Type    : MySQL
 Source Server Version : 80032
 Source Host           : localhost:3306
 Source Schema         : campus_share

 Target Server Type    : MySQL
 Target Server Version : 80032
 File Encoding         : 65001

 Date: 30/10/2025 12:49:18
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for borrow_apply
-- ----------------------------
DROP TABLE IF EXISTS `borrow_apply`;
CREATE TABLE `borrow_apply`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `item_id` bigint(0) NOT NULL COMMENT '物品ID',
  `user_id` bigint(0) NOT NULL COMMENT '申请人ID',
  `apply_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '申请时间',
  `expected_borrow_time` date NULL DEFAULT NULL COMMENT '期望借用时间',
  `expected_return_time` date NULL DEFAULT NULL COMMENT '期望归还时间',
  `status` tinyint(0) NOT NULL DEFAULT 0 COMMENT '状态（0-待审核，1-已同意，2-已拒绝）',
  `reject_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '拒绝原因',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_apply_item`(`item_id`) USING BTREE,
  INDEX `fk_apply_user`(`user_id`) USING BTREE,
  CONSTRAINT `borrow_apply_ibfk_1` FOREIGN KEY (`item_id`) REFERENCES `item` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `borrow_apply_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '借用申请表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of borrow_apply
-- ----------------------------
INSERT INTO `borrow_apply` VALUES (1, 1, 3, '2025-10-29 03:17:47', '2025-11-01', '2025-11-15', 1, '', '2025-10-29 03:17:47', NULL);
INSERT INTO `borrow_apply` VALUES (2, 2, 2, '2025-10-29 03:17:47', '2025-11-02', '2025-11-09', 0, '', '2025-10-29 03:17:47', NULL);
INSERT INTO `borrow_apply` VALUES (3, 1, 4, '2025-10-29 18:19:28', '2025-10-29', '2025-11-13', 0, '', '2025-10-29 18:19:28', NULL);
INSERT INTO `borrow_apply` VALUES (4, 2, 4, '2025-10-29 18:44:16', '2025-10-29', '2025-11-05', 0, '', '2025-10-29 18:44:16', NULL);
INSERT INTO `borrow_apply` VALUES (5, 3, 4, '2025-10-29 20:22:56', '2025-10-29', '2025-11-01', 0, '', '2025-10-29 20:22:56', NULL);
INSERT INTO `borrow_apply` VALUES (6, 3, 3, '2025-10-29 20:33:15', '2025-10-29', '2025-11-01', 0, '', '2025-10-29 20:33:15', NULL);
INSERT INTO `borrow_apply` VALUES (7, 13, 4, '2025-10-30 04:25:26', '2025-10-30', '2025-11-06', 0, '', '2025-10-30 04:25:26', NULL);
INSERT INTO `borrow_apply` VALUES (8, 15, 7, '2025-10-30 07:22:03', '2025-10-30', '2025-11-06', 0, '', '2025-10-30 07:22:03', NULL);

-- ----------------------------
-- Table structure for borrow_record
-- ----------------------------
DROP TABLE IF EXISTS `borrow_record`;
CREATE TABLE `borrow_record`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `apply_id` bigint(0) NOT NULL COMMENT '借用申请ID',
  `start_time` date NOT NULL COMMENT '实际借用开始时间',
  `end_time` date NOT NULL COMMENT '计划归还时间',
  `return_time` date NULL DEFAULT NULL COMMENT '实际归还时间',
  `status` tinyint(0) NOT NULL DEFAULT 0 COMMENT '状态（0-借用中，1-已归还，2-超期未还）',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_apply_id`(`apply_id`) USING BTREE,
  CONSTRAINT `borrow_record_ibfk_1` FOREIGN KEY (`apply_id`) REFERENCES `borrow_apply` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '借用记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of borrow_record
-- ----------------------------
INSERT INTO `borrow_record` VALUES (1, 1, '2025-11-01', '2025-11-15', NULL, 0, '2025-10-29 03:17:47', NULL);

-- ----------------------------
-- Table structure for comment
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `item_id` bigint(0) NOT NULL COMMENT '物品ID',
  `user_id` bigint(0) NOT NULL COMMENT '评价人ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '评价内容',
  `score` tinyint(0) NOT NULL COMMENT '评分（1-5分）',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_delete` tinyint(0) NOT NULL DEFAULT 0 COMMENT '是否删除（0-未删，1-已删）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_comment_item`(`item_id`) USING BTREE,
  INDEX `fk_comment_user`(`user_id`) USING BTREE,
  CONSTRAINT `comment_ibfk_1` FOREIGN KEY (`item_id`) REFERENCES `item` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `comment_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '物品评价表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of comment
-- ----------------------------
INSERT INTO `comment` VALUES (1, 1, 3, '教材很新，内容完整，非常推荐！', 5, '2025-10-29 03:17:47', NULL, 0);
INSERT INTO `comment` VALUES (2, 2, 2, '篮球手感很好，满意', 4, '2025-10-29 03:17:47', NULL, 0);

-- ----------------------------
-- Table structure for item
-- ----------------------------
DROP TABLE IF EXISTS `item`;
CREATE TABLE `item`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '物品名称',
  `item_type_id` bigint(0) NOT NULL COMMENT '物品分类ID',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '物品描述',
  `status` tinyint(0) NOT NULL DEFAULT 0 COMMENT '状态（0-待审核，1-可借用，2-已借出，3-已下架）',
  `user_id` bigint(0) NOT NULL COMMENT '发布者ID',
  `borrow_duration` int(0) NULL DEFAULT 7 COMMENT '可借用时长（天，默认7天）',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_delete` tinyint(0) NOT NULL DEFAULT 0 COMMENT '是否删除（0-未删，1-已删）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_item_type`(`item_type_id`) USING BTREE,
  INDEX `fk_item_user`(`user_id`) USING BTREE,
  CONSTRAINT `item_ibfk_1` FOREIGN KEY (`item_type_id`) REFERENCES `item_type` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `item_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '闲置物品表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of item
-- ----------------------------
INSERT INTO `item` VALUES (1, '高等数学教材', 2, '大二上学期教材，9成新', 1, 2, 15, '2025-10-29 03:17:47', NULL, 0);
INSERT INTO `item` VALUES (2, '篮球', 8, '斯伯丁篮球，几乎全新', 1, 3, 7, '2025-10-29 03:17:47', NULL, 0);
INSERT INTO `item` VALUES (3, '无线鼠标', 7, '罗技无线鼠标，续航30天', 1, 2, 3, '2025-10-29 03:17:47', NULL, 0);
INSERT INTO `item` VALUES (4, '测试', 1, '这是一个测试1111111111111', 1, 4, 1, '2025-10-29 03:19:46', '2025-10-30 06:09:48', 0);
INSERT INTO `item` VALUES (5, '测试00000555', 4, '测试00000555', 3, 1, 7, '2025-10-29 05:46:34', '2025-10-29 17:38:50', 0);
INSERT INTO `item` VALUES (6, '测试00000555', 4, '测试00000555', 1, 1, 7, '2025-10-29 05:47:22', '2025-10-30 06:11:21', 0);
INSERT INTO `item` VALUES (7, '测试00000555', 4, '测试00000555', 1, 1, 7, '2025-10-29 05:47:25', '2025-10-30 06:11:21', 0);
INSERT INTO `item` VALUES (8, '测试00000555', 4, '测试00000555', 3, 1, 7, '2025-10-29 05:47:33', '2025-10-29 09:01:28', 0);
INSERT INTO `item` VALUES (10, '车子', 1, '这是车子3333333', 3, 1, 1, '2025-10-29 06:07:49', '2025-10-30 02:16:24', 0);
INSERT INTO `item` VALUES (11, '车子3', 2, '这是一个多年的玩具2222222', 3, 1, 7, '2025-10-29 06:11:19', '2025-10-29 17:38:41', 0);
INSERT INTO `item` VALUES (12, '车子3', 2, '这是一个多年的玩具2222222', 1, 1, 7, '2025-10-29 06:11:31', '2025-10-30 06:09:38', 0);
INSERT INTO `item` VALUES (13, '待完成发得出', 12, '去得到我4334343', 1, 1, 7, '2025-10-29 06:16:11', '2025-10-30 02:16:07', 0);
INSERT INTO `item` VALUES (14, '老房子', 1, '哦哦哦哦哦哦哦哦哦哦哦哦哦哦哦哦哦哦哦哦哦哦哦哦哦哦哦', 3, 1, 7, '2025-10-29 06:57:22', '2025-10-30 02:16:13', 0);
INSERT INTO `item` VALUES (15, '手机', 4, '这是一部0000000000000000新手机', 1, 4, 7, '2025-10-29 08:11:26', '2025-10-30 02:15:48', 0);
INSERT INTO `item` VALUES (16, '小区君臣', 2, '7969E9E9E679E9E', 3, 1, 7, '2025-10-30 01:57:37', '2025-10-30 01:58:43', 0);
INSERT INTO `item` VALUES (17, '猫猫猫', 2, '啦啦啦啦啦啦啦啦啦啦啦啦啦啦啦啦啦', 0, 4, 7, '2025-10-30 06:14:31', '2025-10-30 06:14:31', 0);
INSERT INTO `item` VALUES (18, '别墅', 2, '888888888888888888', 0, 4, 7, '2025-10-30 06:19:53', '2025-10-30 06:19:53', 0);
INSERT INTO `item` VALUES (19, '房子房子', 2, '888888888888888888', 0, 4, 7, '2025-10-30 06:21:15', '2025-10-30 06:21:15', 0);
INSERT INTO `item` VALUES (20, '房子房子', 2, '888888888888888888', 0, 4, 7, '2025-10-30 06:27:40', '2025-10-30 06:27:40', 0);
INSERT INTO `item` VALUES (21, '房子房子', 1, '888888888888888888', 0, 4, 7, '2025-10-30 06:30:33', '2025-10-30 06:30:33', 0);
INSERT INTO `item` VALUES (22, '房子房子', 12, '888888888888888888', 0, 4, 7, '2025-10-30 06:32:04', '2025-10-30 06:32:04', 0);
INSERT INTO `item` VALUES (23, '不iUI', 3, '66666666666666666666', 0, 4, 7, '2025-10-30 07:00:26', '2025-10-30 07:00:26', 0);

-- ----------------------------
-- Table structure for item_image
-- ----------------------------
DROP TABLE IF EXISTS `item_image`;
CREATE TABLE `item_image`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `item_id` bigint(0) NOT NULL COMMENT '物品ID',
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '图片路径',
  `is_main` tinyint(0) NOT NULL DEFAULT 0 COMMENT '是否主图（0-否，1-是）',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_image_item`(`item_id`) USING BTREE,
  CONSTRAINT `item_image_ibfk_1` FOREIGN KEY (`item_id`) REFERENCES `item` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 32 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '物品图片表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of item_image
-- ----------------------------
INSERT INTO `item_image` VALUES (1, 1, '/images/books/math.jpg', 1, '2025-10-29 03:17:47');
INSERT INTO `item_image` VALUES (2, 1, '/images/books/math2.jpg', 0, '2025-10-29 03:17:47');
INSERT INTO `item_image` VALUES (3, 2, '/images/sports/basketball.jpg', 1, '2025-10-29 03:17:47');
INSERT INTO `item_image` VALUES (4, 3, '/images/electronics/mouse.jpg', 1, '2025-10-29 03:17:47');
INSERT INTO `item_image` VALUES (5, 4, 'http://localhost:8080/images/baf3965e-d807-4710-b80c-a2cc6df44d84.png', 0, '2025-10-29 03:19:46');
INSERT INTO `item_image` VALUES (6, 4, 'http://localhost:8080/images/307b4f9e-5973-4bd2-9648-66c7db975857.png', 1, '2025-10-29 03:19:46');
INSERT INTO `item_image` VALUES (7, 5, 'http://localhost:8080/images/c4d9d477-842e-4166-8fc4-390463d9d514.png', 1, '2025-10-29 05:46:34');
INSERT INTO `item_image` VALUES (8, 6, 'http://localhost:8080/images/dc5b9160-b1b3-4e2e-ab29-864b48b27547.png', 1, '2025-10-29 05:47:22');
INSERT INTO `item_image` VALUES (9, 7, 'http://localhost:8080/images/c58f7f8d-9336-4a97-96be-7264dd0424ae.png', 1, '2025-10-29 05:47:25');
INSERT INTO `item_image` VALUES (10, 8, 'http://localhost:8080/images/3f8dff0e-9d98-4bc5-ac20-ca91d6d7ab4a.png', 1, '2025-10-29 05:47:33');
INSERT INTO `item_image` VALUES (11, 10, 'http://localhost:8080/images/9cf1362f-00c2-4119-af95-74b26c4a331c.png', 1, '2025-10-29 06:07:49');
INSERT INTO `item_image` VALUES (12, 10, 'http://localhost:8080/images/c87397d0-528d-47dc-99a2-524694e0a8ab.png', 0, '2025-10-29 06:07:49');
INSERT INTO `item_image` VALUES (13, 11, 'http://localhost:8080/images/fde3f935-1dd0-4e04-a79d-1e42c9b02ff4.png', 1, '2025-10-29 06:11:19');
INSERT INTO `item_image` VALUES (14, 11, 'http://localhost:8080/images/55eca798-5c47-40dd-a4e0-56bc2f938cff.png', 0, '2025-10-29 06:11:19');
INSERT INTO `item_image` VALUES (15, 11, 'http://localhost:8080/images/b8a07953-986c-4780-990d-858a304021b6.png', 0, '2025-10-29 06:11:19');
INSERT INTO `item_image` VALUES (16, 12, 'http://localhost:8080/images/b1f89e38-5fee-44ab-9672-5c6afe4a42e4.png', 1, '2025-10-29 06:11:31');
INSERT INTO `item_image` VALUES (17, 12, 'http://localhost:8080/images/1274700c-9b26-4c8e-833f-951df7e1df7a.png', 0, '2025-10-29 06:11:31');
INSERT INTO `item_image` VALUES (18, 12, 'http://localhost:8080/images/12879129-9eb9-47b2-93f4-f777efd86c12.png', 0, '2025-10-29 06:11:31');
INSERT INTO `item_image` VALUES (19, 13, 'http://localhost:8080/images/009b22d7-47ab-43ea-bf65-c07361059d04.png', 1, '2025-10-29 06:16:11');
INSERT INTO `item_image` VALUES (20, 14, 'http://localhost:8080/images/ac78ee04-93a1-450b-9c29-6b2647303931.png', 1, '2025-10-29 06:57:23');
INSERT INTO `item_image` VALUES (21, 15, 'http://localhost:8080/images/fe41cb21-24e1-4ae6-be32-204f4f77781b.png', 1, '2025-10-29 08:11:26');
INSERT INTO `item_image` VALUES (22, 15, 'http://localhost:8080/images/ebf91a4f-5542-4c27-a26d-a0ba7763e2e5.png', 0, '2025-10-29 08:11:26');
INSERT INTO `item_image` VALUES (23, 15, 'http://localhost:8080/images/42175fc3-1577-4518-bf76-b79b24f98b0a.png', 0, '2025-10-29 08:11:26');
INSERT INTO `item_image` VALUES (24, 16, 'http://localhost:8080/images/142b90c9-6617-4a0e-bdd6-87296faa8f34.png', 1, '2025-10-30 01:57:37');
INSERT INTO `item_image` VALUES (25, 16, 'http://localhost:8080/images/3dd9ea6b-4807-46a2-a9ff-edd03b726a3c.png', 0, '2025-10-30 01:57:37');
INSERT INTO `item_image` VALUES (26, 17, 'http://localhost:8080/images/1c288c9f-67ba-4c81-b61b-ab9a10de73bc.png', 1, '2025-10-30 06:14:31');
INSERT INTO `item_image` VALUES (27, 18, 'http://localhost:8080/images/806a38f9-5bce-4555-8a44-1ca85e12c0c8.png', 1, '2025-10-30 06:19:53');
INSERT INTO `item_image` VALUES (28, 19, 'http://localhost:8080/images/edccbe84-1148-45ed-8b7f-3737c8ddd5d4.png', 1, '2025-10-30 06:21:15');
INSERT INTO `item_image` VALUES (29, 20, 'http://localhost:8080/images/5e4ea14d-864b-41b1-91d6-6dac4616c41b.png', 1, '2025-10-30 06:27:40');
INSERT INTO `item_image` VALUES (30, 21, 'http://localhost:8080/images/fd9390a6-10cf-4f42-a3cc-7b31258466ad.png', 1, '2025-10-30 06:30:33');
INSERT INTO `item_image` VALUES (31, 22, 'http://localhost:8080/images/0b0872c8-a1bb-4dfa-b8be-bd93fc9ab09e.png', 1, '2025-10-30 06:32:04');
INSERT INTO `item_image` VALUES (32, 23, 'http://localhost:8080/images/0e57821a-cb43-469e-a619-1eee41d012ba.png', 1, '2025-10-30 07:00:26');

-- ----------------------------
-- Table structure for item_type
-- ----------------------------
DROP TABLE IF EXISTS `item_type`;
CREATE TABLE `item_type`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `type_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称（如：课本、电器）',
  `parent_id` bigint(0) NULL DEFAULT 0 COMMENT '父分类ID（0表示一级分类）',
  `sort_order` int(0) NULL DEFAULT 0 COMMENT '排序序号（值越小越靠前）',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_type_name_parent`(`type_name`, `parent_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '物品分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of item_type
-- ----------------------------
INSERT INTO `item_type` VALUES (1, '课本教材', 0, 1, '2025-10-27 00:37:05', NULL);
INSERT INTO `item_type` VALUES (2, '生活用品', 0, 2, '2025-10-27 00:37:05', NULL);
INSERT INTO `item_type` VALUES (3, '运动器材', 0, 3, '2025-10-27 00:37:05', NULL);
INSERT INTO `item_type` VALUES (4, '电子设备', 0, 4, '2025-10-27 00:37:05', NULL);
INSERT INTO `item_type` VALUES (5, '专业工具', 0, 5, '2025-10-27 00:37:05', NULL);
INSERT INTO `item_type` VALUES (6, '考研资料', 1, 1, '2025-10-27 00:37:05', NULL);
INSERT INTO `item_type` VALUES (7, '专业课本', 1, 2, '2025-10-27 00:37:05', NULL);
INSERT INTO `item_type` VALUES (8, '宿舍用品', 2, 1, '2025-10-27 00:37:05', NULL);
INSERT INTO `item_type` VALUES (9, '小家电', 4, 1, '2025-10-27 00:37:05', NULL);
INSERT INTO `item_type` VALUES (10, '数码产品', 4, 2, '2025-10-27 00:37:05', NULL);
INSERT INTO `item_type` VALUES (11, '健身器材', 3, 1, '2025-10-27 00:37:05', NULL);
INSERT INTO `item_type` VALUES (12, '办公用品', 0, 6, '2025-10-29 03:17:47', NULL);
INSERT INTO `item_type` VALUES (13, '文具', 6, 1, '2025-10-29 03:17:47', NULL);
INSERT INTO `item_type` VALUES (14, '体育用品', 3, 2, '2025-10-29 03:17:47', NULL);
INSERT INTO `item_type` VALUES (15, '乐器', 0, 7, '2025-10-29 03:17:47', NULL);

-- ----------------------------
-- Table structure for message
-- ----------------------------
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(0) NOT NULL COMMENT '接收用户ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '消息标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息内容',
  `is_read` tinyint(0) NOT NULL DEFAULT 0 COMMENT ' 是否已读（0-未读，1-已读）',
  `message_type` tinyint(0) NOT NULL COMMENT '消息类型（0-借用通知，1-归还通知，2-系统通知）',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_message_user`(`user_id`) USING BTREE,
  CONSTRAINT `message_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of message
-- ----------------------------
INSERT INTO `message` VALUES (1, 1, '欢迎使用', '欢迎加入校园共享平台，您可以发布闲置物品或借用他人物品', 0, 2, '2025-10-27 00:37:05', NULL);
INSERT INTO `message` VALUES (2, 2, '借用申请功', '您申请的篮球已通过审核，请联系发布者领取', 0, 0, '2025-10-29 03:17:47', NULL);
INSERT INTO `message` VALUES (3, 3, '物品被申请', '您发布的高等数学教材被学生3申请借用', 0, 0, '2025-10-29 03:17:47', NULL);
INSERT INTO `message` VALUES (4, 2, '系统通知', '您的物品已成功发布', 1, 2, '2025-10-29 03:17:47', NULL);

-- ----------------------------
-- Table structure for permission
-- ----------------------------
DROP TABLE IF EXISTS `permission`;
CREATE TABLE `permission`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `perm_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '权限名称（如：物品审核、用户管理）',
  `permission_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '权限关键字（如：item:audit）',
  `parent_id` bigint(0) NULL DEFAULT 0 COMMENT '父权限ID（0表示顶级权限）',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_perm_key`(`permission_key`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '权限表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of permission
-- ----------------------------
INSERT INTO `permission` VALUES (1, '物品审核', 'item:audit', 0, '2025-10-27 00:37:05', NULL);
INSERT INTO `permission` VALUES (2, '用户管理', 'user:manage', 0, '2025-10-27 00:37:05', NULL);
INSERT INTO `permission` VALUES (3, '反馈处理', 'feedback:handle', 0, '2025-10-27 00:37:05', NULL);
INSERT INTO `permission` VALUES (4, '分类管理', 'type:manage', 0, '2025-10-27 00:37:05', NULL);
INSERT INTO `permission` VALUES (5, '发布物品', 'PERMISSION_item:publish', 0, '2025-10-29 03:15:35', NULL);
INSERT INTO `permission` VALUES (6, '查看物品', 'PERMISSION_item:view', 0, '2025-10-29 03:17:47', NULL);
INSERT INTO `permission` VALUES (7, '借用物品', 'PERMISSION_item:borrow', 0, '2025-10-29 03:17:47', NULL);

-- ----------------------------
-- Table structure for role
-- ----------------------------
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称（学生/管理员）',
  `role_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '角色描述',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_name`(`role_name`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of role
-- ----------------------------
INSERT INTO `role` VALUES (1, '学生', '普通用户，可发布、借用物品', '2025-10-27 00:37:05', NULL);
INSERT INTO `role` VALUES (2, '管理员', '系统管理员，负责审核物品、管理用户', '2025-10-27 00:37:05', NULL);

-- ----------------------------
-- Table structure for role_permission
-- ----------------------------
DROP TABLE IF EXISTS `role_permission`;
CREATE TABLE `role_permission`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `role_id` bigint(0) NOT NULL COMMENT '角色ID',
  `permission_id` bigint(0) NOT NULL COMMENT '权限ID',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_perm`(`role_id`, `permission_id`) USING BTREE,
  INDEX `fk_perm_id`(`permission_id`) USING BTREE,
  CONSTRAINT `role_permission_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `role_permission_ibfk_2` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色-权限关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of role_permission
-- ----------------------------
INSERT INTO `role_permission` VALUES (1, 2, 3, '2025-10-27 00:37:05');
INSERT INTO `role_permission` VALUES (2, 2, 1, '2025-10-27 00:37:05');
INSERT INTO `role_permission` VALUES (3, 2, 4, '2025-10-27 00:37:05');
INSERT INTO `role_permission` VALUES (4, 2, 2, '2025-10-27 00:37:05');
INSERT INTO `role_permission` VALUES (8, 1, 5, '2025-10-29 03:15:35');
INSERT INTO `role_permission` VALUES (9, 1, 7, '2025-10-29 03:17:47');
INSERT INTO `role_permission` VALUES (10, 1, 6, '2025-10-29 03:17:47');
INSERT INTO `role_permission` VALUES (12, 2, 7, '2025-10-29 03:17:47');
INSERT INTO `role_permission` VALUES (13, 2, 5, '2025-10-29 03:17:47');
INSERT INTO `role_permission` VALUES (14, 2, 6, '2025-10-29 03:17:47');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名（登录用）',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码（BCrypt加密）',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '真实姓名',
  `student_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '学号',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '手机号码',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '电子邮箱',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '头像路径',
  `role_id` bigint(0) NOT NULL COMMENT '角色ID',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `is_delete` tinyint(0) NOT NULL DEFAULT 0 COMMENT '是否删除（0-未删，1-已删）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username`) USING BTREE,
  UNIQUE INDEX `uk_student_id`(`student_id`) USING BTREE,
  INDEX `fk_user_role`(`role_id`) USING BTREE,
  CONSTRAINT `user_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'admin', '$2a$10$AjMFVlPSSesPkhe4ufqzOeEfVKDp600m/zt8rw3rMOMjh.gd11EDC', '', '', '', '', '', 2, '2025-10-27 00:37:05', '2025-10-27 18:48:43', 0);
INSERT INTO `user` VALUES (2, 'student1', '$2a$10$VJ8tQ9l4Q4eX8D7c6B5dRO5F3s2G1h0j9k8i7f6e5d', '张三', '2023001', '', '', '', 1, '2025-10-27 00:37:05', NULL, 0);
INSERT INTO `user` VALUES (3, 'testuser', '$2a$10$U8vfYnRrnzOITNhqv9ybxesBxv7WKhIrPiVE5pXiug5qaBC/9wJQ.', '测试用户', '2023001001', '13800138000', 'test@example.com', '', 1, '2025-10-27 14:15:46', '2025-10-27 14:15:46', 0);
INSERT INTO `user` VALUES (4, '小东呀', '$2a$10$PVmFVYQewP/hwLTp4QP7M.8BhXlyXpbZwMkVC1RFLZxOSNp1oHWd.', '转让费工作日', '20250223', '13586954789', '18211390888@qq.com', '', 1, '2025-10-28 23:33:37', '2025-10-29 20:22:21', 0);
INSERT INTO `user` VALUES (5, 'student2', '$2a$10$VJ8tQ9l4Q4eX8D7c6B5dRO5F3s2G1h0j9k8i7f6e5d', '李四', '2023002', '13800138002', 'lisi@example.com', '', 1, '2025-10-29 03:17:47', NULL, 0);
INSERT INTO `user` VALUES (6, 'student3', '$2a$10$VJ8tQ9l4Q4eX8D7c6B5dRO5F3s2G1h0j9k8i7f6e5d', '王五', '2023003', '13800138003', 'wangwu@example.com', '', 1, '2025-10-29 03:17:47', NULL, 0);
INSERT INTO `user` VALUES (7, '123', '$2a$10$/obaknhPpKn5pxm9mvpx8eFwhPO8.aN191rfsHjOncf8Ir.o529Um', '', '20368', '18679653468', '', '', 1, '2025-10-30 07:21:19', '2025-10-30 07:21:19', 0);

SET FOREIGN_KEY_CHECKS = 1;
