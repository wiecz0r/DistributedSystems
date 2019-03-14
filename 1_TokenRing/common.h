//
// Created by Szymon on 11.03.2019.
//

#ifndef INC_1_TOKENRING_TOKEN_H

#define INC_1_TOKENRING_TOKEN_H

#define MAX_BUFF_SIZE 1024

#define MULTICAST_IP "224.2.2.2"
#define MULTICAST_PORT 9000

typedef enum ClientStatus {
    FREE,
    HAS_TOKEN
} ClientStatus;

typedef struct Client {
    char ID[MAX_BUFF_SIZE];
    ClientStatus status;
    int is_first;
} Client;

typedef enum MessageType {
    JOIN_REQUEST,
    JOIN_INPROGRESS,
    NORMAL,
    EMPTY
} MessageType;

typedef struct Message {
    MessageType type;
    char msg[MAX_BUFF_SIZE];
} Message;


typedef struct Token {
    char ip_address[MAX_BUFF_SIZE];
    int port_source;
    int port_destination;
    Message message;
} Token;

char *tokenType(MessageType type);

void Send_to_LOGGER(Token token, Client client, int client_port);

#endif //INC_1_TOKENRING_TOKEN_H
