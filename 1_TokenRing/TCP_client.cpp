//
// Created by Szymon on 14.03.2019.
//

#include "TCP_client.h"
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

void TCP_init_sockets();

void TCP_init_sockaddrs();

void TCP_bind_listen_own_socket();

void TCP_add_client();

void TCP_create_message();

void TCP_handle_recevied_tokens();

void TCP(Client user, int port, int n_port, char *n_ip) {
    client = user;
    client_port = port;
    neighbour_port = n_port;
    strcpy(neighbour_ip, n_ip);

    TCP_init_sockets();
    TCP_init_sockaddrs();
    TCP_bind_listen_own_socket();
    if (!client.is_first) {
        TCP_add_client();
    }
    pthread_mutex_init(&mx, nullptr);
    TCP_create_message();
    TCP_handle_recevied_tokens();
}

void TCP_init_sockets() {
    //CLIENT
    if ((own_socket_fd = socket(AF_INET, SOCK_STREAM, 0)) < 0) {
        perror("Error when creating own_socket");
        exit(EXIT_FAILURE);
    } else {
        printf("Own socket created\n");
    }
}

void TCP_init_sockaddrs() {
    own_sockaddr.sin_family = AF_INET;
    own_sockaddr.sin_addr.s_addr = htonl(INADDR_ANY);
    own_sockaddr.sin_port = htons(client_port);

    neighbour_sockaddr.sin_family = AF_INET;
    neighbour_sockaddr.sin_port = htons(neighbour_port);
    neighbour_sockaddr.sin_addr.s_addr = inet_addr(neighbour_ip);
}

void TCP_bind_listen_own_socket() {
    int bind_result = bind(own_socket_fd, (const struct sockaddr *) &own_sockaddr, sizeof(own_sockaddr));
    if (bind_result < 0) {
        perror("Error when binding own_socket\n");
        exit(EXIT_FAILURE);
    } else {
        printf("Own_socket bind completed successfully!\n");
    }
    int listen_result = listen(own_socket_fd, 10);
    if (listen_result < 0) {
        perror("Error when 'listen' on own_socket\n");
        exit(EXIT_FAILURE);
    }
}

void TCP_send_token(Token token) {
    sleep(1);
    if ((neighbour_socket_fd = socket(AF_INET, SOCK_STREAM, 0)) < 0) {
        perror("Error when creating neighbour_socket");
        exit(EXIT_FAILURE);
    }
    if (connect(neighbour_socket_fd, (const struct sockaddr *) &neighbour_sockaddr, sizeof(neighbour_sockaddr)) < 0) {
        perror("Error when trying to connect");
        exit(EXIT_FAILURE);

    }

    if (send(neighbour_socket_fd, &token, sizeof(token), 0) != sizeof(token)) {
        perror("ERROR WHEN SENDING");
    }

    if (close(neighbour_socket_fd) < 0) {
        perror("Error when closing neighbour fd");
        exit(EXIT_FAILURE);
    }
}

void TCP_add_client() {
    Token firstToken;
    strcpy(firstToken.ip_address, CLIENT_IP);
    firstToken.port_destination = neighbour_port;
    firstToken.port_source = client_port;
    firstToken.message.type = JOIN_REQUEST;
    TCP_send_token(firstToken);
    if (client.status == HAS_TOKEN) {
        Token emptyToken = firstToken;
        emptyToken.message.type = EMPTY;
        TCP_send_token(emptyToken);
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

void TCP_create_message() {
    pthread_t result;
    if (pthread_create(&result, nullptr, message_console, nullptr) != 0) {
        perror("Error when creating message_console thread\n");
        exit(EXIT_FAILURE);
    } else {
        printf("Created new message_console thread\n");
    }
}

void TCP_handle_recevied_tokens() {
    Token token;
    while (true) {

        int new_neighbour_fd = accept(own_socket_fd, nullptr, nullptr);
        if (new_neighbour_fd < 0) {
            perror("Error when called function accept on neighbour socket\n");
            exit(EXIT_FAILURE);
        }

        ssize_t receive = recv(new_neighbour_fd, &token, sizeof(token), 0);
        if ((int) receive != sizeof(token)) {
            perror("Error when recvfrom\n");
            exit(EXIT_FAILURE);
        }

        if (close(new_neighbour_fd) < 0) {
            perror("Error when closing new_neighbour_fd!\n");
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
                    TCP_send_token(token);
                } else {
                    TCP_send_token(token);
                }
                pthread_mutex_unlock(&mx);
                break;
            case NORMAL:
                if (token.port_destination == client_port) {
                    printf("FROM: port %d, msg: %s\n", token.port_source, token.message.msg);
                } else if (token.port_source == client_port) {
                    printf("Token has not reached its destination. Deleting...\n");
                } else {
                    TCP_send_token(token);
                }
                Token newToken;
                newToken.message.type = EMPTY;
                TCP_send_token(newToken);
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
                    TCP_send_token(emptyToken);
                } else {
                    TCP_send_token(token);
                }
                break;
            default:
                break;
        }
    }
}