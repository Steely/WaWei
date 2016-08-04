/*删除原本已存在的列，这个语句要小心执行，有可能已有的例中有数据了*/
/*ALTER TABLE `_table_name_` DROP `sync_state`,DROP `sync_id`,DROP `sync_create_time`,DROP `sync_update_time`,DROP `sync_time`;*/

/*为表添加必须要的列，包括了sync_id主键和 sync_state 索引*/
ALTER TABLE `_table_name_` ADD `sync_state` tinyint(3)  NOT NULL DEFAULT 1 COMMENT '同步状态，1.新建 2.正常 3.修改',ADD `sync_id` varchar(36) DEFAULT NULL COMMENT '同步唯一标识',ADD `sync_create_time` timestamp NULL DEFAULT NULL COMMENT '创建时间', ADD `sync_update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',ADD `sync_time` timestamp NULL DEFAULT NULL COMMENT '同步时间', ADD INDEX _table_name__sync_state_index(`sync_state`), ADD UNIQUE KEY _table_name__sync_id_unique_index(`sync_id`);

/* 这个update 语句也要注意执行，如果之前已经存在同步的一些列，要小心处理 */
UPDATE `_table_name_` SET `sync_state`=2;

#
#添加触发器
# 1. 为insert 操作自动添加 sync_id 的值为UUID(), sync_create_time 的值为 current_timestamp()
# 2. 为update 操作条件性的修改 sync_state 为3 ，sync_update_time 条件性的修改为 current_timestamp()
# 3. 发生delete 操作后为sync_delete_row表插入记录数据
#

DELIMITER //
DROP TRIGGER IF EXISTS _table_name__sync_before_insert;
CREATE TRIGGER _table_name__sync_before_insert BEFORE INSERT ON `_table_name_`
FOR EACH ROW
BEGIN
	IF NEW.sync_state <> 2 AND NEW.sync_id IS NULL THEN
		SET NEW.sync_id = UUID(), NEW.sync_create_time=current_timestamp();
	END IF;
END //

DROP TRIGGER IF EXISTS _table_name__sync_before_update;
CREATE TRIGGER _table_name__sync_before_update BEFORE UPDATE ON `_table_name_`
FOR EACH ROW
BEGIN
	IF NEW.sync_time = OLD.sync_time AND OLD.sync_state = 2  THEN
		SET NEW.sync_state = 3;
	END IF;
	IF OLD.sync_time IS NULL OR NEW.sync_time = OLD.sync_time THEN
		SET NEW.sync_update_time = current_timestamp();
	END IF;
END //
# 
# after insert or update
#
DROP TRIGGER IF EXISTS _table_name__sync_after_insert;
CREATE TRIGGER _table_name__sync_after_insert AFTER INSERT ON `_table_name_`
FOR EACH ROW
BEGIN
	IF NEW.sync_state <> 2 THEN
		UPDATE `sync_table_notic` set `update_time`=current_timestamp() WHERE `table_name`='_table_name_';
	END IF;
END //

DROP TRIGGER IF EXISTS _table_name__sync_after_update;
CREATE TRIGGER _table_name__sync_after_update AFTER UPDATE ON `_table_name_`
FOR EACH ROW
BEGIN
	IF NEW.sync_state<>2 THEN
		UPDATE `sync_table_notic` set `update_time`=current_timestamp() WHERE `table_name`='_table_name_';
	END IF;
END //

DROP TRIGGER IF EXISTS _table_name__sync_after_delete;
CREATE TRIGGER _table_name__sync_after_delete AFTER DELETE ON `_table_name_`
FOR EACH ROW
BEGIN
     IF OLD.sync_id is not null THEN
        INSERT INTO sync_delete_row(`table_name`,`sync_id`,`delete_time`,`team_id`,`status`) VALUES('_table_name_',OLD.sync_id,NOW(),OLD.team_id,1);
     END IF;
END //

DELIMITER;