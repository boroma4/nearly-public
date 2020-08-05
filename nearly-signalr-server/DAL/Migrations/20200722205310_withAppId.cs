using Microsoft.EntityFrameworkCore.Migrations;

namespace DAL.Migrations
{
    public partial class withAppId : Migration
    {
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropTable(
                name: "Friends");

            migrationBuilder.AlterColumn<string>(
                name: "UserId",
                table: "Users",
                maxLength: 36,
                nullable: false,
                oldClrType: typeof(string),
                oldType: "varchar(255) CHARACTER SET utf8mb4");

            migrationBuilder.AddColumn<string>(
                name: "AppUserId",
                table: "Users",
                maxLength: 12,
                nullable: false,
                defaultValue: "");

            migrationBuilder.AlterColumn<string>(
                name: "MessageId",
                table: "Messages",
                maxLength: 36,
                nullable: false,
                oldClrType: typeof(string),
                oldType: "varchar(255) CHARACTER SET utf8mb4");

            migrationBuilder.CreateTable(
                name: "UserRelationships",
                columns: table => new
                {
                    RequesterId = table.Column<string>(maxLength: 36, nullable: false),
                    ResponderId = table.Column<string>(maxLength: 36, nullable: false),
                    RequesterName = table.Column<string>(nullable: false),
                    ResponderName = table.Column<string>(nullable: false),
                    Status = table.Column<int>(nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_UserRelationships", x => new { x.RequesterId, x.ResponderId });
                    table.ForeignKey(
                        name: "FK_UserRelationships_Users_RequesterId",
                        column: x => x.RequesterId,
                        principalTable: "Users",
                        principalColumn: "UserId",
                        onDelete: ReferentialAction.Cascade);
                    table.ForeignKey(
                        name: "FK_UserRelationships_Users_ResponderId",
                        column: x => x.ResponderId,
                        principalTable: "Users",
                        principalColumn: "UserId",
                        onDelete: ReferentialAction.Cascade);
                });

            migrationBuilder.CreateIndex(
                name: "IX_UserRelationships_ResponderId",
                table: "UserRelationships",
                column: "ResponderId");
        }

        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropTable(
                name: "UserRelationships");

            migrationBuilder.DropColumn(
                name: "AppUserId",
                table: "Users");

            migrationBuilder.AlterColumn<string>(
                name: "UserId",
                table: "Users",
                type: "varchar(255) CHARACTER SET utf8mb4",
                nullable: false,
                oldClrType: typeof(string),
                oldMaxLength: 36);

            migrationBuilder.AlterColumn<string>(
                name: "MessageId",
                table: "Messages",
                type: "varchar(255) CHARACTER SET utf8mb4",
                nullable: false,
                oldClrType: typeof(string),
                oldMaxLength: 36);

            migrationBuilder.CreateTable(
                name: "Friends",
                columns: table => new
                {
                    RequesterId = table.Column<string>(type: "varchar(255) CHARACTER SET utf8mb4", nullable: false),
                    ResponderId = table.Column<string>(type: "varchar(255) CHARACTER SET utf8mb4", nullable: false),
                    RequesterName = table.Column<string>(type: "longtext CHARACTER SET utf8mb4", nullable: false),
                    ResponderName = table.Column<string>(type: "longtext CHARACTER SET utf8mb4", nullable: false),
                    Status = table.Column<string>(type: "longtext CHARACTER SET utf8mb4", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_Friends", x => new { x.RequesterId, x.ResponderId });
                    table.ForeignKey(
                        name: "FK_Friends_Users_RequesterId",
                        column: x => x.RequesterId,
                        principalTable: "Users",
                        principalColumn: "UserId",
                        onDelete: ReferentialAction.Cascade);
                    table.ForeignKey(
                        name: "FK_Friends_Users_ResponderId",
                        column: x => x.ResponderId,
                        principalTable: "Users",
                        principalColumn: "UserId",
                        onDelete: ReferentialAction.Cascade);
                });

            migrationBuilder.CreateIndex(
                name: "IX_Friends_ResponderId",
                table: "Friends",
                column: "ResponderId");
        }
    }
}
