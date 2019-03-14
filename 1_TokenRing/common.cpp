//
// Created by Szymon on 12.03.2019.
//
#include <sys/socket.h>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <netinet/in.h>
#include <arpa/inet.h>
#include "common.h"

char *tokenType(MessageType type) {
    switch (type) {
        case EMPTY:
            return (char *) "EMPTY";
        case NORMAL:
            return (char *) "NORMAL";
        case JOIN_REQUEST:
            return (char *) "JOIN_REQUEST";
        case JOIN_INPROGRESS:
            return (char *) "JOIN_IN_PROGRESS";
        default:
            return (char *) "unknown";
    }
}


void Send_to_LOGGER(Token token, Client client, int client_port) {
    int multicast_socket_fd;
    static struct sockaddr_in multicast_sockaddr;
    if ((multicast_socket_fd = socket(AF_INET, SOCK_DGRAM, 0)) < 0) {
        perror("Error when creating multicast_socket");
        exit(EXIT_FAILURE);
    }

    multicast_sockaddr.sin_family = AF_INET;
    multicast_sockaddr.sin_port = htons(MULTICAST_PORT);
    multicast_sockaddr.sin_addr.s_addr = inet_addr(MULTICAST_IP);

    char msg[MAX_BUFF_SIZE];
    sprintf(msg, "Client [%s] on port (%d) received a token [%s]\n", client.ID, client_port,
            tokenType(token.message.type));
    sendto(multicast_socket_fd, msg, strlen(msg), 0, (const struct sockaddr *) &multicast_sockaddr,
           sizeof(multicast_sockaddr));
}