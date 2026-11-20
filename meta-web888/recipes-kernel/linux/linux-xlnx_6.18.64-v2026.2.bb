# SPDX-License-Identifier: GPL-3.0-or-later
# Xilinx's 2026.2 kernel (linux-xlnx xlnx_rebase_v6.18_LTS, 6.18.40; the
# recipe as meta-xilinx's wrynose-next has it) with the stable updates up
# to 6.18.64 on top: about 3400 of the CVEs open in 6.18.40 are fixed in
# 6.18.41 to 6.18.64, which Xilinx hasn't merged yet (6.18.64: 205 more,
# 2026-11-20).
#
# linux-6.18.64-stable.patch.xz is stable v6.18.64 merged into Xilinx's
# tag, as one commit; scripts/kernel-stable.sh makes it again from the two
# public trees. Where Xilinx has its own version of a file and the merge
# conflicts, Xilinx's file is kept and the stable changes to it are left
# out (the list is in the patch's message). Drop this recipe when
# meta-xilinx has a kernel at least this new.
LINUX_VERSION = "6.18.64"
YOCTO_META ?= "git://git.yoctoproject.org/yocto-kernel-cache;type=kmeta;name=meta;branch=yocto-6.18;destsuffix=yocto-kmeta"
KBRANCH = "xlnx_rebase_v6.18_LTS"
SRCREV = "85261a88dc955839e3dabe5339b3a8d86ec545db"
SRCREV_meta = "f202157d297182244bde71c8ed8b6d4946deec8f"

KCONF_AUDIT_LEVEL = "0"

include recipes-kernel/linux/linux-xlnx.inc

# CVE exclusions (meta-xilinx's; its per-version list only restates
# what the version already tells)
include recipes-kernel/linux/cve-exclusion-linux-xlnx.inc

PV .= "+v2026.2"

# ours, then linux-xlnx.inc's own files (linux-xlnx-kmeta, ...) next to
# it in meta-xilinx-core
FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:${@os.path.dirname(bb.utils.which(d.getVar('BBPATH'), 'recipes-kernel/linux/linux-xlnx.inc'))}/linux-xlnx:"
SRC_URI += "file://linux-6.18.64-stable.patch.xz"
# the version check reads the Makefile: after the patch makes it 6.18.64
addtask kernel_version_sanity_check after do_patch

# Fixed in stable after 6.18.40 only in files where Xilinx's version is
# kept (above), so not fixed here; none of them is in use on a Web-888.
CVE_STATUS[CVE-2026-80655] = "not-applicable-config: ZynqMP power management (drivers/soc/xilinx/zynqmp_power.c), not built for Zynq-7000"
CVE_STATUS[CVE-2026-80743] = "not-applicable-config: Xilinx audio formatter (sound/soc/xilinx), not built"
CVE_STATUS[CVE-2026-89739] = "not-applicable-config: USB dwc3 (drivers/usb/dwc3), not built (Zynq-7000 has ChipIdea USB)"
CVE_STATUS[CVE-2026-90030] = "not-applicable-config: USB dwc3 (drivers/usb/dwc3), not built (Zynq-7000 has ChipIdea USB)"
CVE_STATUS[CVE-2026-90214] = "not-applicable-config: Xilinx audio formatter (sound/soc/xilinx), not built"
CVE_STATUS[CVE-2026-98767] = "not-applicable-config: USB dwc3 (drivers/usb/dwc3), not built (Zynq-7000 has ChipIdea USB)"
CVE_STATUS[CVE-2026-99028] = "not-applicable-config: AXI Ethernet (drivers/net/ethernet/xilinx/xilinx_axienet*), built but no such device: the FPGA design has none, the board's Ethernet is the PS GEM"
CVE_STATUS[CVE-2026-89788] = "not-applicable-config: ksmbd, the in-kernel SMB server (fs/smb/server), not built (CONFIG_SMB_SERVER is not set)"

# Not fixed in 6.18.64 yet; the mainline fix applies as it is
SRC_URI += "file://CVE-2026-72397.patch"

# Open in 6.18.63 (the daily scan), checked against this kernel's
# configuration (/proc/config.gz on a unit) and the Makefiles that build
# each fix's files (CVSS 7.0 or more 2026-11-13, the rest 2026-11-17:
# whether each fix's files were compiled, in a build tree of this
# kernel). Not built here:
CVE_STATUS_GROUPS += "CVE_STATUS_WEB888_K_BPF CVE_STATUS_WEB888_K_NETFS CVE_STATUS_WEB888_K_SMB \
    CVE_STATUS_WEB888_K_OCFS2 CVE_STATUS_WEB888_K_AMD CVE_STATUS_WEB888_K_FB"
CVE_STATUS_WEB888_K_BPF = "CVE-2026-74354 CVE-2026-98376 CVE-2026-98869 CVE-2026-98813 CVE-2026-98789 CVE-2026-98599 CVE-2026-98589 CVE-2026-98628 CVE-2026-93270 CVE-2026-93135 CVE-2026-93044 CVE-2026-53106"
CVE_STATUS_WEB888_K_BPF[status] = "not-applicable-config: the bpf() syscall, its maps and verifier are not built (CONFIG_BPF_SYSCALL off)"
CVE_STATUS_WEB888_K_NETFS = "CVE-2026-64160 CVE-2026-98118"
CVE_STATUS_WEB888_K_NETFS[status] = "not-applicable-config: netfs and the network file systems using it are not built (CONFIG_NETFS_SUPPORT, 9P_FS, AFS_FS, CIFS off)"
CVE_STATUS_WEB888_K_SMB = "CVE-2026-99381 CVE-2026-99749 CVE-2026-99792 CVE-2026-89630 CVE-2026-89632 CVE-2026-98115 CVE-2026-99275 CVE-2026-99272 CVE-2026-97563 CVE-2026-31710 CVE-2026-98125 CVE-2026-98124"
CVE_STATUS_WEB888_K_SMB[status] = "not-applicable-config: the SMB client and server are not built (CONFIG_CIFS, CONFIG_SMB_SERVER off)"
CVE_STATUS_WEB888_K_OCFS2 = "CVE-2026-90320 CVE-2026-90321"
CVE_STATUS_WEB888_K_OCFS2[status] = "not-applicable-config: OCFS2 is not built (CONFIG_OCFS2_FS off)"
CVE_STATUS_WEB888_K_AMD = "CVE-2026-99620 CVE-2026-68103 CVE-2026-68447 CVE-2026-74419 CVE-2026-98436 CVE-2026-89805 CVE-2026-98926 CVE-2026-98144 CVE-2026-80657 CVE-2026-43446 CVE-2026-23295 CVE-2026-43131 CVE-2026-98106"
CVE_STATUS_WEB888_K_AMD[status] = "not-applicable-config: GPU and accelerator drivers are not built (CONFIG_DRM_AMDGPU, DRM_ACCEL_AMDXDNA, DRM_GPUSVM off)"
CVE_STATUS_WEB888_K_FB = "CVE-2026-80579 CVE-2026-80580 CVE-2026-80785"
CVE_STATUS_WEB888_K_FB[status] = "not-applicable-config: the framebuffer core is not built (CONFIG_FB_CORE off)"
CVE_STATUS[CVE-2025-71074] = "not-applicable-config: USB gadget FunctionFS is not built (CONFIG_USB_F_FS off)"
CVE_STATUS[CVE-2026-100072] = "not-applicable-config: ACPI is not built (an ARM device-tree system)"
CVE_STATUS[CVE-2026-43042] = "not-applicable-config: MPLS is not built (CONFIG_MPLS off)"
CVE_STATUS[CVE-2026-53024] = "not-applicable-config: greybus (staging) is not built"
CVE_STATUS[CVE-2026-63977] = "not-applicable-config: the DPLL subsystem and zl3073x are not built (CONFIG_DPLL off)"
CVE_STATUS[CVE-2026-74752] = "not-applicable-config: SCTP is not built (CONFIG_IP_SCTP off)"
CVE_STATUS[CVE-2026-89610] = "not-applicable-config: the old ntfs driver (fs/ntfs) is not in this tree; ntfs3 is not built either"
CVE_STATUS[CVE-2026-89611] = "not-applicable-config: the old ntfs driver (fs/ntfs) is not in this tree; ntfs3 is not built either"
CVE_STATUS[CVE-2026-90237] = "not-applicable-config: netfilter is not built (CONFIG_NETFILTER off)"
CVE_STATUS[CVE-2026-98364] = "not-applicable-config: IPsec (XFRM) is not built (CONFIG_XFRM off)"
CVE_STATUS[CVE-2022-1247] = "not-applicable-config: the ROSE protocol is not built (CONFIG_ROSE off)"
CVE_STATUS[CVE-2023-3397] = "not-applicable-config: JFS is not built (CONFIG_JFS_FS off)"
CVE_STATUS[CVE-2019-14899] = "not-applicable-config: needs a VPN tunnel interface to probe; TUN and WireGuard are not built"
# Built, but out of an attacker's reach on this system:
CVE_STATUS[CVE-2021-3864] = "mitigated: needs core dumps of a SUID program's descendants; fs.suid_dumpable is 0 and systemd sets kernel.core_pattern to |/bin/false (no coredump handler), and root is the only account with a login"
CVE_STATUS[CVE-2026-53091] = "not-reachable: a malformed GSO packet has to come from TUN/TAP or virtio (not built) or a raw packet socket (CAP_NET_RAW: root and systemd-networkd only); forwarding is off. The mainline fix needs a transmit-path refactor 6.18 doesn't have"
CVE_STATUS[CVE-2026-89584] = "not-reachable: needs direct I/O with crafted vectors against a bio-based block device; here only dm-verity during a RAUC install, whose nodes are root's, and root is the only account with a login"

# The rest of the scan (2026-11-17, all below CVSS 7.0 or unscored).
# Drivers for hardware a Web-888 doesn't have, not built:
CVE_STATUS_GROUPS += "CVE_STATUS_WEB888_K_HW CVE_STATUS_WEB888_K_FS CVE_STATUS_WEB888_K_ARCH"
CVE_STATUS_WEB888_K_HW = "CVE-2026-99716 CVE-2026-99720 CVE-2026-99746 CVE-2026-98966 CVE-2026-98946 CVE-2026-93047 CVE-2026-98824 CVE-2026-98810 \
    CVE-2026-98338 CVE-2026-98333 CVE-2026-98332 CVE-2026-98815 CVE-2026-98814 CVE-2026-98153 CVE-2023-6238 \
    CVE-2026-98617 CVE-2026-98562 CVE-2026-98288 CVE-2026-98355 CVE-2026-98292 CVE-2026-90098 CVE-2026-89862 \
    CVE-2026-74272 CVE-2026-43095"
CVE_STATUS_WEB888_K_HW[status] = "not-applicable-config: a driver for hardware the board doesn't have, not built (bonding, vc4/v3d GPUs, Wi-Fi: CFG80211/MAC80211, NVMe, fbnic, libwx, stmmac, InfiniBand, Bluetooth, sparx5, qla2xxx, CXL, SoundWire SDCA, x86 coretemp, chipcap2, Intel SOF all off)"
CVE_STATUS_WEB888_K_FS = "CVE-2026-98250 CVE-2026-99756 CVE-2026-99679 CVE-2026-99369 CVE-2026-98960 CVE-2026-89772 CVE-2026-80899 CVE-2026-72377"
CVE_STATUS_WEB888_K_FS[status] = "not-applicable-config: the file system or file server is not built (CONFIG_BTRFS_FS, EROFS_FS, AFS_FS, XFS_FS, NFSD off)"
CVE_STATUS_WEB888_K_ARCH = "CVE-2026-99337 CVE-2026-99472 CVE-2026-99493 CVE-2026-99495 CVE-2026-99586 CVE-2026-99125 CVE-2026-97526 CVE-2026-98652 CVE-2026-90211 CVE-2026-93181 CVE-2026-98551"
CVE_STATUS_WEB888_K_ARCH[status] = "not-applicable-platform: s390, x86 or arm64 code, or KVM (which 32-bit ARM no longer has); this is a 32-bit ARM (Zynq-7000) kernel"
CVE_STATUS[CVE-2026-98816] = "not-applicable-config: constant blinding is part of the BPF JIT, which is not built (CONFIG_BPF_JIT off)"
CVE_STATUS[CVE-2026-98938] = "not-applicable-config: Rust support is not built (CONFIG_RUST off)"
CVE_STATUS[CVE-2026-98972] = "not-applicable-config: cpusets are not built (CONFIG_CPUSETS off)"
CVE_STATUS[CVE-2026-98802] = "not-applicable-config: IPv6 segment routing is not built (CONFIG_IPV6_SEG6_LWTUNNEL off)"
CVE_STATUS[CVE-2026-93216] = "not-applicable-config: page_owner is not built (CONFIG_PAGE_OWNER off)"
CVE_STATUS[CVE-2026-68086] = "not-applicable-config: khugepaged is not built (no CONFIG_TRANSPARENT_HUGEPAGE on 32-bit ARM)"
CVE_STATUS[CVE-2026-46017] = "not-applicable-config: the deferred split queue is part of transparent huge pages, which are not built (CONFIG_TRANSPARENT_HUGEPAGE off)"
CVE_STATUS[CVE-2026-43326] = "not-applicable-config: sched_ext is not built (CONFIG_SCHED_CLASS_EXT off)"
CVE_STATUS[CVE-2026-43174] = "not-applicable-config: io_uring zero-copy receive is not built (CONFIG_IO_URING_ZCRX off)"
CVE_STATUS[CVE-2026-98427] = "not-applicable-config: CoDel is not built (CONFIG_NET_SCH_CODEL, NET_SCH_FQ_CODEL and MAC80211 off)"
CVE_STATUS[CVE-2026-74342] = "not-applicable-config: needs an LSM with a kernfs_init_security hook (SELinux); CONFIG_SECURITY is off"
CVE_STATUS[CVE-2026-98285] = "not-applicable-config: bridge VLAN filtering is not built (CONFIG_BRIDGE_VLAN_FILTERING off), and the bridge module is not installed in the image"
CVE_STATUS[CVE-2026-46153] = "not-applicable-config: 802.1Q is built as a module (CONFIG_VLAN_8021Q=m) that the image doesn't install"
CVE_STATUS[CVE-2021-3714] = "not-applicable-config: memory deduplication is not built (CONFIG_KSM off)"
CVE_STATUS[CVE-2023-6240] = "not-applicable-config: the kernel's RSA is not built (CONFIG_CRYPTO_RSA off)"
CVE_STATUS[CVE-2020-10774] = "not-applicable-config: /proc/sys/kernel/rh_features is in Red Hat's kernels only"
CVE_STATUS[CVE-2026-98983] = "not-reachable: unshare() of a namespace needs CAP_SYS_ADMIN (no user namespaces: CONFIG_NAMESPACES off) and then a failed allocation; root is the only account with a login"
CVE_STATUS[CVE-2026-98747] = "not-reachable: on unregistering a serial port that has no port device: unbinding the UART driver (root, through sysfs) or a failed probe; the board's UART is the Zynq PS UART, always present"
CVE_STATUS[CVE-2026-98558] = "not-reachable: needs a CPU taken offline (root, through sysfs); both CPUs stay online"
CVE_STATUS[CVE-2010-4563] = "not-reachable: tells whether an interface is in promiscuous mode; none is (no tcpdump or other sniffer in the image)"
# The scan of 2026-11-20 (unscored yet), the ones 6.18.64 doesn't fix:
# memory-management features that aren't built
CVE_STATUS_GROUPS += "CVE_STATUS_WEB888_K_MM"
CVE_STATUS_WEB888_K_MM = "CVE-2026-99643 CVE-2026-99660 CVE-2026-99669 CVE-2026-99652 CVE-2026-99701 CVE-2026-99650 CVE-2026-99727 CVE-2026-99729"
CVE_STATUS_WEB888_K_MM[status] = "not-applicable-config: in transparent huge pages, userfaultfd, KSM or device-private memory, none of them built (CONFIG_TRANSPARENT_HUGEPAGE, USERFAULTFD, KSM, DEVICE_PRIVATE off)"
CVE_STATUS[CVE-2026-99595] = "not-applicable-config: the throttle state is CFS bandwidth control's, which is not built (CONFIG_CFS_BANDWIDTH off)"
CVE_STATUS[CVE-2026-99674] = "not-reachable: the board has no CAN interface: the Zynq's CAN controllers are disabled in its device tree (no can netdevice to send to)"
CVE_STATUS_GROUPS += "CVE_STATUS_WEB888_K_SWAP"
CVE_STATUS_WEB888_K_SWAP = "CVE-2026-99736 CVE-2026-99738"
CVE_STATUS_WEB888_K_SWAP[status] = "not-reachable: shmem swap-in; the image sets up no swap (only root could add one, and root is the only account with a login)"
CVE_STATUS[CVE-1999-0524] = "upstream-wontfix: Linux answers ICMP timestamp requests (RFC 792) by design and never address mask requests; the clock it gives away is UTC, which the receiver shows anyway"
