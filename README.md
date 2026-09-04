# GayOS Linux

GayOS là Arch Linux-based desktop distribution với Caelestia Dots, Btrfs, Snapper, và Hyprland.

![GayOS](https://archive.org/download/gayos-2026.09.02-x86_64/gayos-2026.09.02-x86_64.iso)

## Features

- **Desktop**: Hyprland Wayland compositor
- **Dotfiles**: Full Caelestia Dots với custom branding
- **Filesystem**: Btrfs với Snapper snapshots
- **Boot**: GRUB với custom splash
- **Installer**: Zenity-based GUI installer
- **Tools**: Fastfetch, Kitty, Pipewire, và nhiều tool khác

## Download

[Download ISO](https://archive.org/download/gayos-2026.09.02-x86_64/gayos-2026.09.02-x86_64.iso) (2.1 GB)

## Cài đặt

1. Boot từ USB với file ISO
2. Chạy `sudo gayos-install`
3. Follow hướng dẫn trong installer
4. Reboot và enjoy!

## Default credentials

- **Username**: do bạn chọn trong installer
- **Password**: do bạn chọn trong installer

## Build từ source

```bash
# Clone repo
git clone https://github.com/yourusername/gayos.git
cd gayos

# Build ISO
sudo mkarchiso -v -w ../work -o ../out .
```

## Packages

- base, linux, linux-firmware
- hyprland, xdg-desktop-portal-hyprland
- kitty, fastfetch, vim
- greetd, greetd-tuigreet
- btrfs-progs, snapper
- pipewire, pipewire-pulse, wireplumber
- NetworkManager, openssh
- paru (AUR helper)
- Caelestia CLI, Shell, và Dots

## License

GayOS sử dụng các package từ Arch Linux và AUR. Xem license của từng package.

## Support

- Discord: https://discord.gg/BGDCFCmMBk
- Issues: https://github.com/yourusername/gayos/issues

## Credits

- Arch Linux: https://archlinux.org
- Caelestia: https://caelestia.gg
- Hyprland: https://hyprland.org
