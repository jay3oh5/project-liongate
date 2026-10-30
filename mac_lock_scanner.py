import asyncio
from bleak import BleakScanner

async def main():
    print("Mac BLE scanner started. Waiting for lock advertisement...", flush=True)
    def detection_callback(device, advertisement_data):
        name = device.name or advertisement_data.local_name or "Unknown"
        uuids = [str(u).lower() for u in advertisement_data.service_uuids]
        print(f"Detected: {name} [{device.address}] RSSI={advertisement_data.rssi} UUIDs={uuids}", flush=True)

    scanner = BleakScanner(detection_callback=detection_callback)
    await scanner.start()
    try:
        await asyncio.sleep(15)
    finally:
        await scanner.stop()
    print("Scanner stopped.", flush=True)

if __name__ == "__main__":
    asyncio.run(main())
