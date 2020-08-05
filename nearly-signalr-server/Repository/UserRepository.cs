using System;
using System.Threading.Tasks;
using DAL;
using Domain;

namespace Repository
{
    public class UserRepository
    {
        private readonly AppDbContext _context;

        public UserRepository(AppDbContext ctx)
        {
            _context = ctx;
        }

        public User? GetUser(string id)
        {
            if (id.Length > 0)
            {
                var user = _context.Users.Find(id);
                return user;
            }
            return null;
        }

        public async Task UpdateStatusIndicatorAsync(User user, StatusIndicator status)
        {
            user.StatusIndicator = status;
            _context.Users.Update(user);
            await _context.SaveChangesAsync();
        }
 
    }
}