//
// Created by Szymon on 11.03.2019.
//

#include "UDP_client.h"
#include "common.h"
#include <iostream>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <ctime>
#include <zconf.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <queue>
#include <stdlib.h>
#include <csignal>

using std::queue;

#define CLIENT_IP "127.0.0.1"

static int client_port;
static int neighbour_port;
static char neighbour_ip[MAX_BUFF_SIZE];

static Client client;

static int neighbour_socket_fd;
static int own_socket_fd;

static struct sockaddr_in own_sockaddr;
static struct sockaddr_in neighbour_sockaddr;

static queue<Token> TokenQueue;
static pthread_mutex_t mx;

void UDP_init_sockets();

void UDP_init_sockaddrs();

void UDP_bind_own_socket();

void UDP_add_client();

void UDP_create_message();

void UDP_handle_recevied_tokens();

void UDP(Client user, int port, int n_port, char *n_ip) {
    client = user;
    client_port = port;
    neighbour_port = n_port;
    strcpy(neighbour_ip, n_ip);

    UDP_init_sockets();
    UDP_init_sockaddrs();
    UDP_bind_own_socket();
    if (!client.is_first) {
        UDP_add_client();
    }
    pthread_mutex_init(&mx, nullptr);
    UDP_create_message();
    UDP_handle_recevied_tokens();
}

void UDP_init_sockets() {
    //NEIGHBOUR
    if ((neighbour_socket_fd = socket(AF_INET, SOCK_DGRAM, 0)) < 0) {
        perror("Error when creating multicast_socket");
        exit(EXIT_FAILURE);
    } else {
        printf("Neighbour socket created\n");
    }


    //CLIENT
    if ((own_socket_fd = socket(AF_INET, SOCK_DGRAM, 0)) < 0) {
        perror("Error when creating own_socket");
        exit(EXIT_FAILURE);
    } else {
        printf("Own socket created\n");
    }
}

void UDP_init_sockaddrs() {
    own_sockaddr.sin_family = AF_INET;
    own_sockaddr.sin_addr.s_addr = htonl(INADDR_ANY);
    own_sockaddr.sin_port = htons(client_port);

    neighbour_sockaddr.sin_family = AF_INET;
    neighbour_sockaddr.sin_port = htons(neighbour_port);
    neighbour_sockaddr.sin_addr.s_addr = inet_addr(neighbour_ip);
}

void UDP_bind_own_socket() {
    int bind_result = bind(own_socket_fd, (const struct sockaddr *) &own_sockaddr, sizeof(own_sockaddr));
    if (bind_result < 0) {
        perror("Error when binding own_socket\n");
        exit(EXIT_FAILURE);
    } else {
        printf("Own_socket bind completed successfully!\n");
    }
}

void UDP_send_token(Token token) {
    sleep(1);
    if (sendto(neighbour_socket_fd, &token, sizeof(token), 0, (const struct sockaddr *) &neighbour_sockaddr,
               sizeof(neighbour_sockaddr)) != sizeof(token)) {
        perror("ERROR WHEN SENDING");
    }
}

void UDP_add_client() {
    Token firstToken;
    strcpy(firstToken.ip_address, CLIENT_IP);
    firstToken.port_destination = neighbour_port;
    firstToken.port_source = client_port;
    firstToken.message.type = JOIN_REQUEST;
    UDP_send_token(firstToken);
    if (client.status == HAS_TOKEN) {
        Token emptyToken = firstToken;
        emptyToken.message.type = EMPTY;
        UDP_send_token(emptyToken);
    }
}

static void *message_console(void *args) {
    while (true) {
        printf("\nMESSAGE TO (port): ");
        int port;
        std::cin >> port;
        printf("\nMESSAGE CONTENT: ");
        char msg[MAX_BUFF_SIZE];
        std::cin >> msg;
        Token token;
        token.port_source = client_port;
        token.port_destination = port;
        token.message.type = NORMAL;
        strcpy(token.ip_address, CLIENT_IP);
        strcpy(token.message.msg, msg);
        pthread_mutex_lock(&mx);
        TokenQueue.push(token);
        pthread_mutex_unlock(&mx);
    }
}

void UDP_create_message() {
    pthread_t result;
    if (pthread_create(&result, nullptr, message_console, nullptr) != 0) {
        perror("Error when creating message_console thread\n");
        exit(EXIT_FAILURE);
    } else {
        printf("Created new message_console thread\n");
    }
}

void UDP_handle_recevied_tokens() {
    Token token;
    while (true) {
        struct sockaddr receivedAddr;
        socklen_t size = sizeof(receivedAddr);
        ssize_t receive = recvfrom(own_socket_fd, &token, sizeof(token), 0, &receivedAddr, &size);
        if ((int) receive != sizeof(token)) {
            perror("Error when recvfrom\n");
            exit(EXIT_FAILURE);
        }
        int msgType = token.message.type;
        Send_to_LOGGER(token, client, client_port);
        switch (msgType) {
            case EMPTY:
                pthread_mutex_lock(&mx);
                if (!TokenQueue.empty()) {
                    token = TokenQueue.front();
                    TokenQueue.pop();
                    UDP_send_token(token);
                } else {
                    UDP_send_token(token);
                }
                pthread_mutex_unlock(&mx);
                break;
            case NORMAL:
                if (token.port_destination == client_port) {
                    printf("FROM: port %d, msg: %s\n", token.port_source, token.message.msg);
                } else if (token.port_source == client_port) {
                    printf("Token has not reached its destination. Deleting...\n");
                } else {
                    UDP_send_token(token);
                }
                Token newToken;
                newToken.message.type = EMPTY;
                UDP_send_token(newToken);
                break;
            case JOIN_REQUEST:
                token.message.type = JOIN_INPROGRESS;
                pthread_mutex_lock(&mx);
                TokenQueue.push(token);
                pthread_mutex_unlock(&mx);
                break;
            case JOIN_INPROGRESS:
                if (token.port_destination == neighbour_port) {
                    if (token.port_source != client_port) {
                        neighbour_port = token.port_source;
                    }
                    Token emptyToken;
                    emptyToken.message.type = EMPTY;
                    neighbour_sockaddr.sin_port = htons(neighbour_port);
                    UDP_send_token(emptyToken);
                } else {
                    UDP_send_token(token);
                }
                break;
            default:
                break;
        }
    }
}




