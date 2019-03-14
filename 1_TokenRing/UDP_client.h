//
// Created by Szymon on 11.03.2019.
//

#ifndef INC_1_TOKENRING_UDP_CLIENT_H
#define INC_1_TOKENRING_UDP_CLIENT_H

#include "common.h"

#define MULTICAST_IP_ADDRESS "224.0.0.1"

void UDP(Client user, int port, int n_port, char *n_ip);

#endif //INC_1_TOKENRING_UDP_CLIENT_H
