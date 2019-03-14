#include <stdio.h>
#include <cstdlib>
#include <cstring>
#include "UDP_client.h"
#include "common.h"
#include "TCP_client.h"

int main(int argc, char **argv) {
    if (argc != 8) {
        printf("CLIENT ID, LISTEN PORT, NEIGHBOUR IP, NEIGHBOUR PORT, CLIENT HAS TOKEN AT START (1/0), UDP/TCP, IS FIRST\n");
        perror("Wrong number of arguments\n");
        exit(EXIT_FAILURE);
    }
//    ARG1 - CLIENT ID
//    ARG2 - LISTEN PORT
//    ARG3 - NEIGHBOUR IP
//    ARG4 - NEIGHBOUR PORT
//    ARG5 - CLIENT HAS TOKEN AT START?
//    ARG6 - UDP OR TCP
//    ARG7 - is first client?
    int port = atoi(argv[2]);
    int n_port = atoi(argv[4]);
    char *n_ip = argv[3];
    Client client;
    strcpy(client.ID, argv[1]);
    client.status = atoi(argv[5]) == 1 ? HAS_TOKEN : FREE;
    char *protocol = argv[6];
    client.is_first = atoi(argv[7]);

    printf("ID: %s, port: %d, neighbour ip & port: %s %d, has token: %d, protocol: %s\n", client.ID, port, n_ip, n_port,
           client.status, protocol);

    if (strcmp(protocol, "UDP") == 0 || strcmp(protocol, "udp") == 0) {
        UDP(client, port, n_port, n_ip);
    } else if (strcmp(protocol, "TCP") == 0 || strcmp(protocol, "tcp") == 0) {
        TCP(client, port, n_port, n_ip);
    } else {
        perror("Wrong protocol! Terminating... \n");
        exit(EXIT_FAILURE);
    }


}