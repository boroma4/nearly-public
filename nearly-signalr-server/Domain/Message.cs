using System;
using System.ComponentModel.DataAnnotations;

namespace Domain
{
    public class Message
    {
        //Locate the message in the database
        [MaxLength(36)]
        public string MessageId { get; set; } = default!;

        //Can be a UserId or a GroupId (To be implemented: Make a possibility for marking
        //a certain message to have an owner - a User sent through a Group)
        public string FromId { get; set; } = default!;

        //Can be a UserId or a GroupId 
        public string ToId { get; set; } = default!;

        //What the message contains
        public string MessageBody { get; set; } = default!;
    }
}