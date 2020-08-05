using System;
using Microsoft.AspNetCore.Http;

namespace PigeonWebApp.Extensions
{
    public  static class HttpContextExtensions
    {
        /// <summary>
        /// Get user id from http context using an extension function
        /// </summary>
        /// <param name="ctx">Http context</param>
        /// <returns>Id as string or an empty string if no id is present</returns>
        public static string GetUserId(this HttpContext ctx)
        {
            try
            {
                ctx.Request.Headers.TryGetValue("Authorization", out var id);
                return id.ToString().Replace("Bearer ", "");;
            }
            catch (NullReferenceException ex)
            {
                Console.WriteLine(ex.StackTrace);
                return "";
            }
        }
    }
}