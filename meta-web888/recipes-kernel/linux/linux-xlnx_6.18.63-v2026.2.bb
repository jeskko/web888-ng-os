# SPDX-License-Identifier: GPL-3.0-or-later
# Xilinx's 2026.2 kernel (linux-xlnx xlnx_rebase_v6.18_LTS, 6.18.40; the
# recipe as meta-xilinx's wrynose-next has it) with the stable updates up
# to 6.18.63 on top: about 3200 of the CVEs open in 6.18.40 are fixed in
# 6.18.41 to 6.18.63, which Xilinx hasn't merged yet.
#
# linux-6.18.63-stable.patch.xz is stable v6.18.63 merged into Xilinx's
# tag, as one commit; scripts/kernel-stable.sh makes it again from the two
# public trees. Where Xilinx has its own version of a file and the merge
# conflicts, Xilinx's file is kept and the stable changes to it are left
# out (the list is in the patch's message). Drop this recipe when
# meta-xilinx has a kernel at least this new.
LINUX_VERSION = "6.18.63"
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
SRC_URI += "file://linux-6.18.63-stable.patch.xz"
# the version check reads the Makefile: after the patch makes it 6.18.63
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

# Not fixed in 6.18.63 yet; the mainline fix applies as it is
SRC_URI += "file://CVE-2026-72397.patch"

# Open in 6.18.63 (the daily scan, CVSS 7.0 or more), checked against this
# kernel's configuration (/proc/config.gz on a unit) and the Makefiles
# that build each fix's files (2026-11-13). Not built here:
CVE_STATUS_GROUPS += "CVE_STATUS_WEB888_K_BPF CVE_STATUS_WEB888_K_NETFS CVE_STATUS_WEB888_K_SMB \
    CVE_STATUS_WEB888_K_OCFS2 CVE_STATUS_WEB888_K_AMD CVE_STATUS_WEB888_K_FB"
CVE_STATUS_WEB888_K_BPF = "CVE-2026-74354 CVE-2026-98376 CVE-2026-98869"
CVE_STATUS_WEB888_K_BPF[status] = "not-applicable-config: the bpf() syscall, its maps and verifier are not built (CONFIG_BPF_SYSCALL off)"
CVE_STATUS_WEB888_K_NETFS = "CVE-2026-64160 CVE-2026-98118"
CVE_STATUS_WEB888_K_NETFS[status] = "not-applicable-config: netfs and the network file systems using it are not built (CONFIG_NETFS_SUPPORT, 9P_FS, AFS_FS, CIFS off)"
CVE_STATUS_WEB888_K_SMB = "CVE-2026-89630 CVE-2026-89632 CVE-2026-98115"
CVE_STATUS_WEB888_K_SMB[status] = "not-applicable-config: the SMB client and server are not built (CONFIG_CIFS, CONFIG_SMB_SERVER off)"
CVE_STATUS_WEB888_K_OCFS2 = "CVE-2026-90320 CVE-2026-90321"
CVE_STATUS_WEB888_K_OCFS2[status] = "not-applicable-config: OCFS2 is not built (CONFIG_OCFS2_FS off)"
CVE_STATUS_WEB888_K_AMD = "CVE-2026-68103 CVE-2026-68447 CVE-2026-74419 CVE-2026-98436 CVE-2026-89805"
CVE_STATUS_WEB888_K_AMD[status] = "not-applicable-config: GPU and accelerator drivers are not built (CONFIG_DRM_AMDGPU, DRM_ACCEL_AMDXDNA, DRM_GPUSVM off)"
CVE_STATUS_WEB888_K_FB = "CVE-2026-80579 CVE-2026-80580"
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
