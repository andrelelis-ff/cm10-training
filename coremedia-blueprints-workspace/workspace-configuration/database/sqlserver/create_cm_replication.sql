CREATE DATABASE [cm_replication]
GO
ALTER DATABASE [cm_replication] SET COMPATIBILITY_LEVEL = 100
GO
ALTER DATABASE [cm_replication] SET ANSI_NULL_DEFAULT OFF
GO
ALTER DATABASE [cm_replication] SET ANSI_NULLS OFF
GO
ALTER DATABASE [cm_replication] SET ANSI_PADDING OFF
GO
ALTER DATABASE [cm_replication] SET ANSI_WARNINGS OFF
GO
ALTER DATABASE [cm_replication] SET ARITHABORT OFF
GO
ALTER DATABASE [cm_replication] SET AUTO_CLOSE OFF
GO
ALTER DATABASE [cm_replication] SET AUTO_CREATE_STATISTICS ON
GO
ALTER DATABASE [cm_replication] SET AUTO_SHRINK OFF
GO
ALTER DATABASE [cm_replication] SET AUTO_UPDATE_STATISTICS ON
GO
ALTER DATABASE [cm_replication] SET CURSOR_CLOSE_ON_COMMIT OFF
GO
ALTER DATABASE [cm_replication] SET CURSOR_DEFAULT GLOBAL
GO
ALTER DATABASE [cm_replication] SET CONCAT_NULL_YIELDS_NULL OFF
GO
ALTER DATABASE [cm_replication] SET NUMERIC_ROUNDABORT OFF
GO
ALTER DATABASE [cm_replication] SET QUOTED_IDENTIFIER OFF
GO
ALTER DATABASE [cm_replication] SET RECURSIVE_TRIGGERS OFF
GO
ALTER DATABASE [cm_replication] SET DISABLE_BROKER
GO
ALTER DATABASE [cm_replication] SET AUTO_UPDATE_STATISTICS_ASYNC OFF
GO
ALTER DATABASE [cm_replication] SET DATE_CORRELATION_OPTIMIZATION OFF
GO
ALTER DATABASE [cm_replication] SET PARAMETERIZATION SIMPLE
GO
ALTER DATABASE [cm_replication] SET READ_WRITE
GO
ALTER DATABASE [cm_replication] SET RECOVERY FULL
GO
ALTER DATABASE [cm_replication] SET MULTI_USER
GO
ALTER DATABASE [cm_replication] SET PAGE_VERIFY CHECKSUM
GO
USE [cm_replication]
GO
IF NOT EXISTS (SELECT name FROM sys.filegroups WHERE is_default=1 AND name = N'PRIMARY') ALTER DATABASE [cm_replication] MODIFY FILEGROUP [PRIMARY] DEFAULT
GO

USE [master]
GO
CREATE LOGIN [cm_replication] WITH PASSWORD=N'cm_replication', DEFAULT_DATABASE=[cm_replication], CHECK_EXPIRATION=OFF, CHECK_POLICY=OFF
GO
USE [cm_replication]
GO
CREATE USER [cm_replication] FOR LOGIN [cm_replication]
GO
USE [cm_replication]
GO
ALTER USER [cm_replication] WITH DEFAULT_SCHEMA=[cm_replication]
GO
USE [cm_replication]
GO
EXEC sp_addrolemember N'db_datareader', N'cm_replication'
GO
USE [cm_replication]
GO
EXEC sp_addrolemember N'db_datawriter', N'cm_replication'
GO
USE [cm_replication]
GO
EXEC sp_addrolemember N'db_ddladmin', N'cm_replication'
GO

USE [cm_replication]
GO
CREATE SCHEMA [cm_replication] AUTHORIZATION [cm_replication]
GO
USE [cm_replication]
GO
ALTER USER [cm_replication] WITH DEFAULT_SCHEMA=[cm_replication]
GO
