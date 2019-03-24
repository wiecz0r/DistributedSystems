import socket
import struct
from datetime import datetime

MULTICAST_IP = '224.2.2.2'
MULTICAST_PORT = 9000

def main():

	sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM, socket.IPPROTO_UDP)
	sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)

	sock.bind((MULTICAST_IP, MULTICAST_PORT))
	mreq = struct.pack("4sl", socket.inet_aton(MULTICAST_IP), socket.INADDR_ANY)

	sock.setsockopt(socket.IPPROTO_IP, socket.IP_ADD_MEMBERSHIP, mreq)

	print("LOGGER STARTED")

	while True:
	    buff, address = sock.recvfrom(1024)
	    print(datetime.now().strftime('%Y-%m-%d %H:%M:%S') + ": " + str(buff, 'utf-8'))

if __name__=="__main__":
	main()