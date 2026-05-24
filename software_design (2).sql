-- phpMyAdmin SQL Dump
-- version 5.2.3
-- https://www.phpmyadmin.net/
--
-- 主机： localhost
-- 生成日期： 2026-05-23 23:27:46
-- 服务器版本： 8.0.35
-- PHP 版本： 8.2.28

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- 数据库： `software_design`
--

-- --------------------------------------------------------

--
-- 表的结构 `activity`
--

CREATE TABLE `activity` (
  `activity_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `user_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `title` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '活动标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '活动内容',
  `recruits_number` int NOT NULL COMMENT '招募人数',
  `school_address` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
  `start_date` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `activity_state` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '活动状态（招募中、进行中、结束、取消）',
  `audit_state` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '审核状态（待审核、通过、未通过）',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `publish_time` datetime NOT NULL COMMENT '发布时间',
  `summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '活动总结',
  `summary_state` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '活动总结状态（未审核、通过、未通过）',
  `summary_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `summary_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
  `summary_audit_state` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0',
  `summary_submit_time` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='支教活动表' ROW_FORMAT=DYNAMIC;

--
-- 转存表中的数据 `activity`
--

INSERT INTO `activity` (`activity_id`, `user_id`, `title`, `content`, `recruits_number`, `school_address`, `start_date`, `end_date`, `activity_state`, `audit_state`, `audit_time`, `publish_time`, `summary`, `summary_state`, `summary_title`, `summary_content`, `summary_audit_state`, `summary_submit_time`) VALUES
('jycj8iiv4u', 'b5ry73126x', 'aaa面包批发', '在这里可以体会到面包批发的快乐', 5, '九天', '2026-05-23', '2026-05-26', '0', '0', NULL, '2026-05-23 21:24:19', NULL, NULL, NULL, NULL, '0', NULL),
('krvc39gn2z', 'f91ghf3ff3', '招生', '的', 5, '但是', '2026-05-29', '2026-05-23', '0', '2', '2026-05-23 21:40:22', '2026-05-23 20:41:50', NULL, NULL, NULL, NULL, '0', NULL),
('la6i1apkra', '08ujwz7pwc', '晋江小学支教', '哈哈', 5, '重庆', '2026-05-23', '2026-05-24', '0', '1', '2026-05-23 15:14:21', '2026-05-23 15:12:40', NULL, NULL, NULL, NULL, '0', NULL),
('tly26osdfq', 'f91ghf3ff3', '南航支教', '1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111', 5, '是', '2026-05-23', '2026-05-23', '0', '1', '2026-05-23 20:26:23', '2026-05-23 19:51:01', NULL, NULL, NULL, NULL, '0', NULL),
('xj1j7z69cg', '08ujwz7pwc', '锦江中学支教', '满', 1, '上传成都', '2026-05-23', '2026-05-23', '0', '1', '2026-05-23 19:49:37', '2026-05-23 19:48:32', NULL, NULL, NULL, NULL, '0', NULL);

-- --------------------------------------------------------

--
-- 表的结构 `administrator`
--

CREATE TABLE `administrator` (
  `user_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `user_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `user_permission` int DEFAULT '3',
  `user_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `register_time` datetime NOT NULL COMMENT '创建时间',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '管理员姓名'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='管理员表' ROW_FORMAT=DYNAMIC;

--
-- 转存表中的数据 `administrator`
--

INSERT INTO `administrator` (`user_id`, `user_password`, `user_permission`, `user_phone`, `register_time`, `user_name`) VALUES
('0987654321', '114514', 3, '13388160000', '2026-05-23 19:58:41', '牢大'),
('1234567890', '886886', 3, '12345678901', '2026-05-23 14:41:17', '曼巴');

-- --------------------------------------------------------

--
-- 表的结构 `comment`
--

CREATE TABLE `comment` (
  `comment_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论内容',
  `publish_time` datetime NOT NULL COMMENT '发布时间',
  `post_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `user_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评论表' ROW_FORMAT=DYNAMIC;

--
-- 转存表中的数据 `comment`
--

INSERT INTO `comment` (`comment_id`, `content`, `publish_time`, `post_id`, `user_id`) VALUES
('bk8h2t1egp', '你好', '2026-05-23 22:29:53', 'vjtfo6w9su', '1234567890');

-- --------------------------------------------------------

--
-- 表的结构 `post`
--

CREATE TABLE `post` (
  `post_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `user_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '帖子标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '帖子内容',
  `audit_state` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '审核状态（未审核、通过、未通过）',
  `publish_time` datetime NOT NULL COMMENT '发布时间',
  `activity_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `audit_time` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='主题帖表' ROW_FORMAT=DYNAMIC;

--
-- 转存表中的数据 `post`
--

INSERT INTO `post` (`post_id`, `user_id`, `title`, `content`, `audit_state`, `publish_time`, `activity_id`, `audit_time`) VALUES
('33f0as8bhm', '08ujwz7pwc', '晋江小学支教', '好啊', '2', '2026-05-23 15:36:36', 'la6i1apkra', '2026-05-23 15:37:56'),
('vjtfo6w9su', 'hownjzh11w', '南航支教', 'what canisay', '1', '2026-05-23 22:27:37', 'tly26osdfq', '2026-05-23 22:29:31');

-- --------------------------------------------------------

--
-- 表的结构 `registration`
--

CREATE TABLE `registration` (
  `registration_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `user_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `activity_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `phone_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `real_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '真实姓名',
  `id_number` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '报名时填写的身份证号',
  `gender` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '性别',
  `degree` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '学历',
  `introduce` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
  `audit_state` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '审核状态（未通过、已通过）',
  `entry_time` datetime NOT NULL COMMENT '报名时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='活动报名表' ROW_FORMAT=DYNAMIC;

--
-- 转存表中的数据 `registration`
--

INSERT INTO `registration` (`registration_id`, `user_id`, `activity_id`, `phone_number`, `real_name`, `id_number`, `gender`, `degree`, `introduce`, `audit_state`, `entry_time`) VALUES
('1hxdwxr522', 'z5vyrb0qut', 'la6i1apkra', '17761317988', '玛卡巴卡', '123456789012345678', '女', '博士', '我是一个好孩子', '1', '2026-05-23 15:31:14'),
('6m1ir8hd8s', '4vfolj72lz', 'xj1j7z69cg', '17766666666', '吴亦鸣', '098765432112345678', '男', '专科', '哈哈', '2', '2026-05-23 19:54:39'),
('e1dapgkbks', '1776131798', 'ivqbt6k58j', '17761317987', '刘奕灵', '123456789012345678', '男', '本科', '南航', '1', '2026-05-22 11:07:14'),
('mxid6pphre', 'hownjzh11w', 'tly26osdfq', '13333333333', '张雪峰', '111111111111111111', '男', '博士', '', '0', '2026-05-23 22:26:20'),
('z33vakam76', 'z5vyrb0qut', 'xj1j7z69cg', '17761317988', '玛卡巴卡', '123456789012345678', '女', '博士', '111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111', '1', '2026-05-23 19:51:35');

-- --------------------------------------------------------

--
-- 表的结构 `school_user`
--

CREATE TABLE `school_user` (
  `user_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `user_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `user_permission` int NOT NULL DEFAULT '2' COMMENT '权限',
  `school_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '学校名称',
  `type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '学校类型',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '学校地址',
  `license` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '办学许可证',
  `principle` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '负责人',
  `user_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `register_time` datetime NOT NULL COMMENT '注册时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学校用户表' ROW_FORMAT=DYNAMIC;

--
-- 转存表中的数据 `school_user`
--

INSERT INTO `school_user` (`user_id`, `user_password`, `user_permission`, `school_name`, `type`, `address`, `license`, `principle`, `user_phone`, `register_time`) VALUES
('08ujwz7pwc', '123123', 2, '晋江学校', '初中', '晋江', 'gui123124', '李云龙', '13882341598', '2026-05-23 14:58:50'),
('25gbf2zy63', '123456', 2, '牛马大学', '小学', '妈妈省', '1234576', '大厦被', '18994747539', '2026-05-23 22:25:12'),
('b5ry73126x', '11111111', 2, '九天', '高中', '九天', '111', '九天', '17797822936', '2026-05-23 21:16:55'),
('f91ghf3ff3', '123456', 2, 'nh', '初中', '四川省', 'gui5454', 'jyf', '13388160787', '2026-05-20 16:24:05'),
('lozdwg111b', '123456', 2, '666', '小学', '合欢', '666', '556', '13851873885', '2026-05-23 22:26:13');

-- --------------------------------------------------------

--
-- 表的结构 `user`
--

CREATE TABLE `user` (
  `user_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `user_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户密码',
  `user_permission` int NOT NULL COMMENT '用户权限（1=管理员, 2=学校, 3=志愿者）',
  `user_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `register_time` datetime NOT NULL COMMENT '注册时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户基础表' ROW_FORMAT=DYNAMIC;

-- --------------------------------------------------------

--
-- 表的结构 `volunteer_user`
--

CREATE TABLE `volunteer_user` (
  `user_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `user_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `user_permission` int NOT NULL DEFAULT '3' COMMENT '权限',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `id_number` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `user_sex` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '性别',
  `user_edu` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '学历',
  `user_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `register_time` datetime NOT NULL COMMENT '注册时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='志愿者用户表' ROW_FORMAT=DYNAMIC;

--
-- 转存表中的数据 `volunteer_user`
--

INSERT INTO `volunteer_user` (`user_id`, `user_password`, `user_permission`, `user_name`, `id_number`, `user_sex`, `user_edu`, `user_phone`, `register_time`) VALUES
('4vfolj72lz', '123456', 1, '吴亦鸣', '098765432112345678', '男', '专科', '17766666666', '2026-05-23 19:54:02'),
('73h84tyn3n', '121212', 1, '廖一兰', '987654321087654321', '男', '本科', '17761315555', '2026-05-23 19:59:34'),
('duysnad2br', '11111111', 1, '金宇凡', '650102200408300011', '男', '本科', '17797822935', '2026-05-23 21:11:52'),
('hownjzh11w', '123123', 1, '张雪碧', '111111111111111111', '男', '博士', '13333333333', '2026-05-23 22:24:28'),
('z5vyrb0qut', '123456', 1, '玛卡巴卡', '123456789012345678', '女', '本科', '17761317988', '2026-05-23 14:57:42');

--
-- 转储表的索引
--

--
-- 表的索引 `activity`
--
ALTER TABLE `activity`
  ADD PRIMARY KEY (`activity_id`) USING BTREE,
  ADD KEY `idx_activity_user_id` (`user_id`) USING BTREE;

--
-- 表的索引 `administrator`
--
ALTER TABLE `administrator`
  ADD PRIMARY KEY (`user_id`) USING BTREE;

--
-- 表的索引 `comment`
--
ALTER TABLE `comment`
  ADD PRIMARY KEY (`comment_id`) USING BTREE,
  ADD KEY `idx_comment_post_id` (`post_id`) USING BTREE,
  ADD KEY `idx_comment_user_id` (`user_id`) USING BTREE;

--
-- 表的索引 `post`
--
ALTER TABLE `post`
  ADD PRIMARY KEY (`post_id`) USING BTREE,
  ADD KEY `idx_post_user_id` (`user_id`) USING BTREE;

--
-- 表的索引 `registration`
--
ALTER TABLE `registration`
  ADD PRIMARY KEY (`registration_id`) USING BTREE,
  ADD KEY `idx_reg_activity_id` (`activity_id`) USING BTREE,
  ADD KEY `idx_reg_user_id` (`user_id`) USING BTREE;

--
-- 表的索引 `school_user`
--
ALTER TABLE `school_user`
  ADD PRIMARY KEY (`user_id`) USING BTREE,
  ADD UNIQUE KEY `uk_license` (`license`) USING BTREE;

--
-- 表的索引 `user`
--
ALTER TABLE `user`
  ADD PRIMARY KEY (`user_id`) USING BTREE;

--
-- 表的索引 `volunteer_user`
--
ALTER TABLE `volunteer_user`
  ADD PRIMARY KEY (`user_id`) USING BTREE,
  ADD UNIQUE KEY `idx_id_number` (`id_number`) USING BTREE;

--
-- 限制导出的表
--

--
-- 限制表 `comment`
--
ALTER TABLE `comment`
  ADD CONSTRAINT `fk_comment_post` FOREIGN KEY (`post_id`) REFERENCES `post` (`post_id`) ON DELETE CASCADE ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
