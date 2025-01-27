/*
 * Copyright (C) 2021 crDroid Android Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#define LOG_TAG "AntiFlickerService"

#include <android-base/file.h>
#include <android-base/logging.h>
#include <android-base/strings.h>

#include "AntiFlicker.h"

namespace vendor {
namespace lineage {
namespace livedisplay {
namespace V2_0 {
namespace implementation {

static constexpr const char* kDcDimmingStatusPath =
        "/sys/devices/platform/soc/ae00000.qcom,mdss_mdp/drm/card0/card0-DSI-1/dimlayer_bl_en";

Return<bool> AntiFlicker::isEnabled() {
    std::string buf;
    if (!android::base::ReadFileToString(kDcDimmingStatusPath, &buf)) {
        LOG(ERROR) << "Failed to read " << kDcDimmingStatusPath;
        return false;
    }
    return std::stoi(android::base::Trim(buf)) == 1;
}

Return<bool> AntiFlicker::setEnabled(bool enabled) {
    if (!android::base::WriteStringToFile((enabled ? "1" : "0"), kDcDimmingStatusPath)) {
        LOG(ERROR) << "Failed to write " << kDcDimmingStatusPath;
        return false;
    }
    return true;
}

}  // namespace implementation
}  // namespace V2_0
}  // namespace livedisplay
}  // namespace lineage
}  // namespace vendor
