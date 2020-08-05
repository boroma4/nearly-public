﻿using System;
 using System.Collections.Generic;
using System.Linq;

namespace Hubs
{
    /// <summary>
    /// Replica of <see cref="https://docs.microsoft.com/en-us/aspnet/signalr/overview/guide-to-the-api/mapping-users-to-connections"/>.
    /// In-memory user id to connections mapper.
    /// </summary>
    /// <typeparam name="T">Type of user id.</typeparam>
    public class HubMapper<T>
    {
        private readonly Dictionary<T, HashSet<string>> _connections =
            new Dictionary<T, HashSet<string>>();

        /// <summary>
        /// Get the number of connections
        /// </summary>
        public int Count => _connections.Count;
        
        //Add a connection to a dictionary
        public void Add(T key, string connectionId)
        {
            lock (_connections)
            {
                if (!_connections.TryGetValue(key, out var connections))
                {
                    connections = new HashSet<string>();
                    _connections.Add(key, connections);
                }

                lock (connections)
                {
                    connections.Add(connectionId);
                }
            }
        }

        /// <summary>
        /// Get IEnumerable of Connections
        /// </summary>
        /// <param name="key"> Get all connections for a specified key</param>
        /// <returns></returns>
        public IEnumerable<string> GetConnections(T key)
        {
            return _connections.TryGetValue(key, out var connections) ? connections : Enumerable.Empty<string>();
        }
        
        /// <summary>
        /// Get all existing connections
        /// </summary>
        /// <returns></returns>
        public IEnumerable<string> GetAllConnections()
        {
            HashSet<string> connections = new HashSet<string>();
            var connectionsEnumerator = _connections.Values;

            foreach (var entry in connectionsEnumerator.SelectMany(conn => conn))
            {
                connections.Add(entry);
            }
            
            return connections.Count !=0 ? connections : Enumerable.Empty<string>();
        }

        /// <summary>
        /// Remove a connection from the dictionary
        /// </summary>
        /// <param name="key">User, which has these connections</param>
        /// <param name="connectionId">Connection to be removed</param>
        public void Remove(T key, string connectionId)
        {
            lock (_connections)
            {
                if (!_connections.TryGetValue(key, out var connections))
                {
                    return;
                }

                lock (connections)
                {
                    connections.Remove(connectionId);

                    if (connections.Count == 0)
                    {
                        _connections.Remove(key);
                    }
                }
            }
        }
    }
}