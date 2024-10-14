# NFC Serial Reader

This module allows reading NFC tags using a serial connection to an RFID reader.

## Installation

To install run:

```bash
pnpm add nfc-serial-reader
```

or

```bash
npm install nfc-serial-reader
```

or

```bash
yarn add nfc-serial-reader
```

## Usage

NFC serial reader module provides the following API:

### `connectNFCSerialReader`

This connects to the NFC reader on the given serial port and specified baud rate, starts reading in
the background and returns a boolean indicating if the connection was successful.

```typescript
import { connectNFCSerialReader } from "nfc-serial-reader";

const connected = await connectNFCSerialReader("/dev/ttyS0", 9600);
```

### `addNFCReadListener`

> Note: This function should be called after `connectNFCSerialReader` has been called.

This adds a listener to the NFC reader, the listener will be called every time a new tag is read.

```typescript
import { addNFCReadListener } from "nfc-serial-reader";

addNFCReadListener((card) => {
	console.log(`Card Type: ${card.cardType}`);
	console.log(`Card ID: ${card.cardId}`);
});
```

The payload of the listener is an object with the following properties:

- `cardType`: The type of the card, this is either "TAG" and "WORKER". These are the only two
  supported card types, if the card type is not one of these two values, then the card type
  is returned as "UNKNOWN".
- `cardId`: The ID of the card which is an integer value.

This returns a function that can be called to remove the listener.

### `disconnectNFCSerialReader`

This disconnects the NFC reader, stops reading and returns a boolean indicating if the
disconnection was successful.

```typescript
import { disconnectNFCSerialReader } from "nfc-serial-reader";

const disconnected = await disconnectNFCSerialReader();
```

### `listSerialPorts`

This returns a list of available serial ports on the device (the options that can be passed to
`connectNFCSerialReader`).

```typescript
import { listSerialPorts } from "nfc-serial-reader";

const ports = await listSerialPorts();
```

### `listBaudRates`

This returns a list of available baud rates that can be passed to `connectNFCSerialReader`.
The baud rates are hardcoded to the following values: `[9600, 19200, 38400, 57600, 115200]`.

```typescript
import { listBaudRates } from "nfc-serial-reader";

const baudRates = listBaudRates();
```
