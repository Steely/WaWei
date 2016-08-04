@echo off
setlocal enabledelayedexpansion
rem 表名可以能过mysqlshow -u root -ppowerall dbname 指令来快速展示，再拷贝到tables.txt
rem 要初化的表全填在tables.txt
set file=tables.txt
rem 初始化后的sql 全放这个文件夹
set folder=tablessql
rem 数据库用户名
set sqlUser=xxx
rem 数据库密码
set sqlPass=xxx
rem 数据ip
set sqlHost=localhost
rem 数据库名
set sqlDb=yc_sync_test
rem 修改数据库的模版
set install_simple=auto_install_table_simple.sql
call :cleanFiles
rem 为要同步的库创建一个标识表
set _sql_executor1=mysql -u %sqlUser% -p%sqlPass% -h %sqlHost% %sqlDb% -e "CREATE TABLE IF NOT EXISTS `sync_table_notic` (`table_name` varchar(30) DEFAULT NULL,`update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,PRIMARY KEY (`table_name`)) ENGINE=MyISAM DEFAULT CHARSET=utf8"
echo %_sql_executor1%
mysql -u %sqlUser% -p%sqlPass% -h %sqlHost% %sqlDb% -e "CREATE TABLE IF NOT EXISTS `sync_table_notic ` (`table_name` varchar(30) DEFAULT NULL,`update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP,PRIMARY KEY (`table_name`)) ENGINE=MyISAM DEFAULT CHARSET=utf8"

rem 为要同步的库创建记录删除数据的表
set _sql_executor2= mysql -u %sqlUser% -p%sqlPass% -h %sqlHost% %sqlDb% -e "CREATE TABLE IF NOT EXISTS `sync_delete_row` (`table_name` varchar(30) NOT NULL DEFAULT '' COMMENT '表名',`sync_id` varchar(36) NOT NULL DEFAULT '' COMMENT '同步id',`team_id` int(11) NOT NULL DEFAULT '0' COMMENT '车场id',`delete_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '删除时间',`status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '同步状态1、待同步 2、同步成功 3、同步失败',`sync_time` timestamp NULL DEFAULT NULL COMMENT '同步时间',`remark` varchar(255) DEFAULT NULL COMMENT '备注，通常存放失败的原因',KEY `synced_index` (`status`),KEY `tname_status_team_id_index` (`table_name`,`status`,`team_id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;"
echo %_sql_executor2%
mysql -u %sqlUser% -p%sqlPass% -h %sqlHost% %sqlDb% -e "CREATE TABLE IF NOT EXISTS `sync_delete_row` (`table_name` varchar(30) NOT NULL DEFAULT '' COMMENT '表名',`sync_id` varchar(36) NOT NULL DEFAULT '' COMMENT '同步id',`team_id` int(11) NOT NULL DEFAULT '0' COMMENT '车场id',`delete_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '删除时间',`status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '同步状态1、待同步 2、同步成功 3、同步失败',`sync_time` timestamp NULL DEFAULT NULL COMMENT '同步时间',`remark` varchar(255) DEFAULT NULL COMMENT '备注，通常存放失败的原因',KEY `synced_index` (`status`),KEY `tname_status_team_id_index` (`table_name`,`status`,`team_id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;"

rem 循环读出要初始化的表
echo "======start generator sql file======"
for /f "delims=" %%i in ('type "%file%"') do ( 
	call :GenerateSqlFile %%i
)
echo "======start execute sql file======"
call :executeSql
goto:EOF

rem 根据表名生成对应的sql文件
:GenerateSqlFile
set tableName=%~1
set old_char=_table_name_
set to_file_name=%folder%/auto_install_%tableName%.sql
set del_file_name=%folder%\auto_install_%tableName%.sql
set _sql_executor=mysql -u %sqlUser% -p%sqlPass% -h %sqlHost% %sqlDb% -e "INSERT INTO `sync_table_notic`(`table_name`) SELECT * FROM (SELECT '%tableName%') AS tmp WHERE NOT EXISTS(SELECT * FROM `sync_table_notic` WHERE `table_name`='%tableName%')"
if exist %del_file_name% del /F %del_file_name%
echo "execute sql : %_sql_executor%"
mysql -u %sqlUser% -p%sqlPass% -h %sqlHost% %sqlDb% -e "INSERT INTO `sync_table_notic`(`table_name`) SELECT * FROM (SELECT '%tableName%') AS tmp WHERE NOT EXISTS(SELECT * FROM `sync_table_notic` WHERE `table_name`='%tableName%')"
if exist %del_file_name% del /F %del_file_name%
for /f "delims=" %%i in ('type "%install_simple%"') do ( 	
	set str=%%i 
	rem 将_table_name_的字符替换成具体的表名
	set "str=!str:%old_char%=%tableName%!" 
	echo !str!>> %to_file_name%
)
echo generator table "%tableName%" sql to file %to_file_name% !
goto:EOF

rem 执行sql 指令
:executeSql
for /R "%folder%" %%s in (*) do (
	echo "execute mysql -u %sqlUser% -p%sqlPass% -h %sqlHost% %sqlDb% < %%s"
	mysql -u %sqlUser% -p%sqlPass% -h %sqlHost% %sqlDb% < %%s
)
goto:EOF
:cleanFiles
for /R "%folder%" %%s in (*) do (
	set _del_file_name=%folder%\%%s
	echo "delete file %_del_file_name%"
	del /F %_del_file_name%
)
goto:EOF