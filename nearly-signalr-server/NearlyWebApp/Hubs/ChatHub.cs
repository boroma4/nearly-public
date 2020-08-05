using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using DAL;
using Domain;
using Hubs;
using Microsoft.AspNetCore.SignalR;
using PigeonWebApp.Extensions;
using Repository;

namespace PigeonWebApp.Hubs
{
    public class ChatHub : Hub
    {
        private readonly AppDbContext _context;
        private readonly UserRepository _repository;
        private static readonly HashSet<string> ConnectionIdsOnCall = new HashSet<string>();
        private static readonly HubMapper<string> Connections = new HubMapper<string>();
        
        private const string UserOnline = "UserOnline";
        private const string UserOffline = "UserOffline";
        private const string UserOnCall = "UserOnCall";


        public ChatHub(AppDbContext context)
        {
            _context = context;
            _repository = new UserRepository(context);
        }

        /// <summary>
        /// Connection to SignalR event
        /// </summary>
        /// <returns></returns>
        public override Task OnConnectedAsync()
        {
            var id = Context.GetHttpContext().GetUserId();
            var user = _repository.GetUser(id);
            if (user == null) throw new ArgumentException("User id was not valid");

            Console.WriteLine($"--> Connection Opened: {Context.ConnectionId} by user: {user.Email}");

            Connections.Add(id, Context.ConnectionId);
            if (!ConnectionIdsOnCall.Contains(Context.ConnectionId))
            {
                _repository.UpdateStatusIndicatorAsync(user, StatusIndicator.Online);

                var friendIds = _context.UserRelationships.GetFriends(id).GetFriendIds(id);
                foreach (var connections in friendIds.Select(friendId => Connections.GetConnections(friendId).ToList()))
                {
                    Clients.Clients(connections).SendAsync(UserOnline, id);
                }
            }
            return base.OnConnectedAsync();
        }

        /// <summary>
        /// Disconnect from SignalR event
        /// </summary>
        /// <param name="exception"></param>
        /// <returns></returns>
        public override Task OnDisconnectedAsync(Exception exception)
        {
            var id = Context.GetHttpContext().GetUserId();

            Console.WriteLine($"--> Connection closed: {Context.ConnectionId} by user: {id}");

            var user = _repository.GetUser(id);
            if (user == null) return base.OnDisconnectedAsync(exception);

            Connections.Remove(id, Context.ConnectionId);
            
            // put user online if he disconnected while on call
            if (ConnectionIdsOnCall.Contains(Context.ConnectionId))
            {
                ConnectionIdsOnCall.Remove(Context.ConnectionId);
                _repository.UpdateStatusIndicatorAsync(user, StatusIndicator.Online);
                
                var fIds = _context.UserRelationships.GetFriends(id).GetFriendIds(id);
                foreach (var connections in fIds.Select(friendId => Connections.GetConnections(friendId).ToList()))
                { 
                    Clients.Clients(connections).SendAsync(UserOnline, id);
                }
            }
            
            // if this was the last device, put user offline
            if (Connections.GetConnections(id).ToList().Count != 0) return base.OnDisconnectedAsync(exception);

            _repository.UpdateStatusIndicatorAsync(user, StatusIndicator.Offline);
            
            var friendIds = _context.UserRelationships.GetFriends(id).GetFriendIds(id);
            foreach (var connections in friendIds.Select(friendId => Connections.GetConnections(friendId).ToList()))
            {
                Clients.Clients(connections).SendAsync(UserOffline, id);
            }

            return base.OnDisconnectedAsync(exception);
        }

        /// <summary>
        /// Simple string data exchange e.g hangup, hold + user id
        /// </summary>
        public async Task RtcMessage(string receiver, object data)
        {
            Console.WriteLine("Got message" + receiver);
            var receiverConnections = Connections.GetConnections(receiver).ToList();
            await Clients.Clients(receiverConnections).SendAsync("RtcMessage", data);
        }

        /// <summary>
        /// ICE exchange
        /// </summary>
        public async Task RtcIce(string receiver, object data)
        {
            Console.WriteLine("Got ice for " + receiver);
            var receiverConnections = Connections.GetConnections(receiver).ToList();
            await Clients.Clients(receiverConnections).SendAsync("RtcIce", data);
            
        }

        /// <summary>
        /// WebRTC session data exchange
        /// </summary>
        public async Task RtcSes(string receiver, object data)
        {
            Console.WriteLine("Got ses for " + receiver);
            var receiverConnections = Connections.GetConnections(receiver).ToList();
            await Clients.Clients(receiverConnections).SendAsync("RtcSes", data);
        }

        /// <summary>
        /// Notify friends that user went on or off call
        /// </summary>
        public async Task UpdateCallStatus(bool callStarted, string connectionId)
        {
            var id = Context.GetHttpContext().GetUserId();
            Console.WriteLine($"{id} updated call status!");
            var user = _repository.GetUser(id);
            if (user == null) throw new ArgumentException("User id was not valid");

            if (callStarted) ConnectionIdsOnCall.Add(connectionId); 
            else ConnectionIdsOnCall.Remove(connectionId);
            
            var methodToUse = callStarted ? UserOnCall : UserOnline;
            var newStatus = callStarted ? StatusIndicator.OnCall : StatusIndicator.Online;
            await _repository.UpdateStatusIndicatorAsync(user, newStatus);

            var friendIds = _context.UserRelationships.GetFriends(id).GetFriendIds(id);
            foreach (var connections in friendIds.Select(friendId => Connections.GetConnections(friendId).ToList()))
            {
                await Clients.Clients(connections).SendAsync(methodToUse, id);
            }
        }
    }
}