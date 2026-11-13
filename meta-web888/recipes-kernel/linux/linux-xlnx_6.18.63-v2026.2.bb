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

FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"
SRC_URI += "file://linux-6.18.63-stable.patch.xz"

# Fixed in stable after 6.18.40 only in files where Xilinx's version is
# kept (above), so not fixed here; none of them is in use on a Web-888.
CVE_STATUS[CVE-2026-80655] = "not-applicable-config: ZynqMP power management (drivers/soc/xilinx/zynqmp_power.c), not built for Zynq-7000"
CVE_STATUS[CVE-2026-80743] = "not-applicable-config: Xilinx audio formatter (sound/soc/xilinx), not built"
CVE_STATUS[CVE-2026-89739] = "not-applicable-config: USB dwc3 (drivers/usb/dwc3), not built (Zynq-7000 has ChipIdea USB)"
CVE_STATUS[CVE-2026-90030] = "not-applicable-config: USB dwc3 (drivers/usb/dwc3), not built (Zynq-7000 has ChipIdea USB)"
CVE_STATUS[CVE-2026-90214] = "not-applicable-config: Xilinx audio formatter (sound/soc/xilinx), not built"
CVE_STATUS[CVE-2026-98767] = "not-applicable-config: USB dwc3 (drivers/usb/dwc3), not built (Zynq-7000 has ChipIdea USB)"
CVE_STATUS[CVE-2026-99028] = "not-applicable-config: AXI Ethernet (drivers/net/ethernet/xilinx/xilinx_axienet*), built but no such device: the FPGA design has none, the board's Ethernet is the PS GEM"
