using Microsoft.EntityFrameworkCore.Migrations;

namespace DAL.Migrations
{
    public partial class InitialCreate : Migration
    {
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AlterColumn<string>(
                name: "AppUserId",
                table: "Users",
                maxLength: 56,
                nullable: true,
                oldClrType: typeof(string),
                oldType: "varchar(12) CHARACTER SET utf8mb4",
                oldMaxLength: 12,
                oldNullable: true);
        }

        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AlterColumn<string>(
                name: "AppUserId",
                table: "Users",
                type: "varchar(12) CHARACTER SET utf8mb4",
                maxLength: 12,
                nullable: true,
                oldClrType: typeof(string),
                oldMaxLength: 56,
                oldNullable: true);
        }
    }
}
