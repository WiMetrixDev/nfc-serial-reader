import { EventEmitter, NativeModulesProxy } from "expo-modules-core";

import NFCSerialReaderModule from "./NFCSerialReaderModule";

import type { Subscription } from "expo-modules-core";

export function connectNFCSerialReader(
	serialPort: string,
	baudRate: number
): boolean {
	return NFCSerialReaderModule.connect(serialPort, baudRate);
}

export function listSerialPorts(): string[] {
	return NFCSerialReaderModule.listSerialPorts();
}

export function listBaudRates(): number[] {
	return NFCSerialReaderModule.listBaudRates();
}

export function disconnectNFCSerialReader(): boolean {
	return NFCSerialReaderModule.disconnect();
}

const emitter = new EventEmitter(
	NFCSerialReaderModule ?? NativeModulesProxy.NFCSerialReader
);

export type NFCReadEventPayload = {
	cardType: "TAG" | "WORKER" | "UNKNOWN";
	cardId: number;
};

export function addNFCReadListener(
	listener: (event: NFCReadEventPayload) => void
): Subscription {
	return emitter.addListener<NFCReadEventPayload>("onRead", listener);
}
