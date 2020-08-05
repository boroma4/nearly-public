using System;
using System.Collections;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace Domain
{
    public class User
    {
        [MaxLength(36)]
        public string UserId { get; set; } = default!;
        
        [MaxLength(56)]
        public string? AppUserId { get; set; } 

        public string UserName { get; set; } = default!;
        public string? UserBio { get; set; }
        public string Email { get; set; } = default!;
        public string? ImageUrl { get; set; }
        public StatusIndicator StatusIndicator { get; set; } = default!;
        
        [InverseProperty(nameof(UserRelationship.Requester))]
        public ICollection<UserRelationship>? UserRelationshipRequesters { get; set; }
        [InverseProperty(nameof(UserRelationship.Responder))]
        public ICollection<UserRelationship>? UserRelationshipResponders { get; set; }
        

    }
}