using System.Collections.Generic;
using System.Linq;
using Domain;
using Helper;
using Microsoft.EntityFrameworkCore;

namespace PigeonWebApp.Extensions
{
    public static class UserRelationshipListExtensions
    {
        public static List<UserRelationship> GetFriends(this DbSet<UserRelationship> relationships, string userId)
        {
            return relationships
                .Where(r => (r.RequesterId == userId || r.ResponderId == userId) && r.Status == UserRelationshipStatus.Accepted)
                .ToList();
        }
        public static List<string> GetFriendIds(this List<UserRelationship> relationships, string userId)
        {
            var friendIds1 = relationships
                .Where(r => r.RequesterId != userId)
                .Select(r => r.RequesterId)
                .ToList();
            
            var friendIds2 = relationships
                .Where(r => r.ResponderId != userId)
                .Select(r => r.ResponderId)
                .ToList();
            
            friendIds1.AddRange(friendIds2);

            return friendIds1;
        }
        
    }
}